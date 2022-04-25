## Layered BFS (BFS Tree + Leader Election)

Extend the simulator you developed in Project 1 to simulate asynchronous networks. The message
transmission time for each link for each message is to be randomly chosen using a uniform distribution
in the range 1 to 12 “time units.” All links are bidirectional and FIFO. (FIFO: If I send two messages
m1 and then m2 to you, then you receive m1 first and then m2.)

Implement the LayeredBFS algorithm for leader election. 

**Input:**
Your program will read in the following information in this order from an input file called input.dat:
- The first line has a single integer and it represents the total number of processes in the system.
- The second third line is the index (1..n) of the root of the BFS tree to be created.
- Lines 3 to n+2 represent the connectivity matrix as a set of n 0’s and 1.s: Line 3+i represents the neighbors of process i: the jth component of this line is a 1 if i and j are neighbors and is 0 of i and j are not neighbors.

**Output:** When the tree has been built, 
- Each process should output, on the screen, one line of data containing its index and its list of children (as indices, again) separated by one space.
- Max Leader ID.
- Compute the total number of messages sent for the run and output the result.

### Sample Input File Content
```text
9
1
0 1 1 0 0 0 0 0 0 
1 0 0 1 1 0 0 0 0 
1 0 0 0 0 1 1 0 0 
0 1 0 0 0 0 0 1 0 
0 1 0 0 0 0 0 0 1 
0 0 1 0 0 0 0 0 0 
0 0 1 0 0 0 0 0 0 
0 0 0 1 0 0 0 0 0 
0 0 0 0 1 0 0 0 0
```

### Sample Input Graph Structure
![Graph](/docs/dc-algos-async/imgs/layered_bfs_sample_input.png)

### Sample Output
Note, that in the diagram above, we are using 1 base index. In the output, we are using 0 based index.

```text
Output : Process ID = 0 Children = [ProcessId{id=2}, ProcessId{id=1}]
Output : Process ID = 1 Children = [ProcessId{id=4}, ProcessId{id=3}]
Output : Process ID = 2 Children = [ProcessId{id=5}, ProcessId{id=6}]
Output : Process ID = 6 Children = []
Output : Process ID = 5 Children = []
Output : Process ID = 3 Children = [ProcessId{id=7}]
Output : Process ID = 4 Children = [ProcessId{id=8}]
Output : Process ID = 7 Children = []
Output : Process ID = 8 Children = []
Leader Id : 8
{PAckPayload=8, TerminatePayload=8, SearchPayload=8, IAmDonePayload=16, NAckPayload=8, NewPhasePayload=16}
```



### Working

The code base contains 3 main sections.

#### 1. ASync Process:

- It is similar to `SyncProcess`. But we don't have `enableNextRount()`. 
Had we had `enableNextRound()` along with `Simulated Async Network (using SharedMemoryBus)`, we would be kind of 
doing things, similar to a running `Synchronous algorithm` on top of `Synchronizer`. 

- For the `layered bfs` project, we are using `initiate()` to start the algorithm from an assigned root.
- We don't have the queue for `PreviousRoundsMessage` and `CurrentRoundsMessages` as we are not having rounds.
- Once initiated, we do an endless while loop, until the `isTerminated` is set to true.
```java
  @Override
  public void run() {
        while (!isTerminated) syncWait();
  }

    public void setTerminated(boolean terminated) {
        isTerminated = terminated;
        syncNotify();
    }
```
We will be waiting, until any update to isTerminated occurs. That update, will happen only via `setTerminated()`.
So that is the only place where we use `syncNotify()`.

- `onReceive` will directly send the message to the listener rather than saving it in `previousRoundQueue`.

#### 2. Service Bus:

It is also similar to the Synchronous scenario. 
- `Scalar Clock`. It is a incrementing counter, which is used to keep track of relative time.
- `PriorityBlockingQueue` : It is used to save delayed messages. When we invoke `send(..., delay)` we add
it to the priority_queue (ordered based on `exitTimeStamp`). Then we check, if the top `elements` in the
priority_queue, is having timestamp >= scalar_clock. If so, we send the top messages. Here we are using a while
loop to check this condition, to ensure that every message with the criteria is send.
- `DelayedMessage`: This class is a wrapper for the original messages, which needs to be delayed. Here we have an
extra parameter called `exitTs` which keeps track of the time when we should process the message. Comparable is
implemented to make PriorityQueue min heap based on `exitTs`.
- tick() : Here the communication channel is having rounds. ticks() is called, at a period set in the Main thread.
Upon tick invoke, we do the priority queue check to see if any message satisfies the processing criteria. The round()
here helps in simplifying things.

#### 3. Layered BFS Algorithm:

Layered BFS builds BFS Tree in Asynchronous general network. The algo is as follows:
- in Round 1: Root sends out `Search Messages` to its neighbours and receives a `PACK`(Positive ACK) or `NACK`.
This information is used to build the layer 1, of our tree.
- When root receives all the ACK messages, it goes to the next phase.
- in the subsequent phases k, the root sends `NewPhaseMessage`. The message goes through the `bfs tree` children 
util the depth k is reached. **NOTE**: If the current node (in the bfs tree) doesn't have any children further, we return
`IAMDone` stating no new neighbour found.
- Upon reaching the depth k, it sends `Search Message` to explore the next layer.
- If the neighbour already has the bfs tree parent assigned, it responds with a `NACK`.
- If the neighbours bfs tree parent is not assigned, it response with `PACK`.
- When the k'th layer, receives all the ACK's it sends an `IAMDone` message to its parent. This message 
convergecasts to the root. This message will also, contain, `isNewNeighbourFound` & `maxProcessIdFound` variables.
On the way up, these variables are assigned to the local state of processes. `isNewNeighbourFound` would be reset,
after the message is send to the parent. But maxProcessIdFound, would be kind of still residing in the Process state.
It doesn't have much significance (as it is based on just one half of the tree), it is there to help us 
find the maxLeaderIdFound, among the received IAM Done messages.
The correct leaderId will be set in the `terminate message`, when we do a final termination broadcast.
- Terminate is called, when the converge cast of ACK or IAMDone in the root, doesn't find any new neighbour.

##### Rounds for SharedBus in  Main Thread 

```java
    // Initiate algorithm from root.
    int root = configFileReader.getRoot();
    LayeredBfsASyncProcess rootProcess = layeredBfsProcesses[root];
    rootProcess.initiate();

    // Endless while loop
    while (true) {

      TimeUtils.sleep(500);

      // Termination check, if no threads are alive, then terminate
      if (isAllThreadsDead(threads)) {
        log(LogLevel.DEBUG, "Layered BFS completed");
        break;
      }

      // Tick
      SharedMemoryBus.tick();
    }

    return rootProcess.getLeaderId();
```