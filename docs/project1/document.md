# Distributed Computing Project 1

Flood Max Leader Eleciton with Termination Detection.

## Group Members

1. Anirudh Kiran - AXK200227
2. Arjun Sunil Kumar - AXS210011
3. Spencer James Williams - SJW160330

## Project 1: Flood Max (Leader Election)

You will develop a simple simulator that simulates a synchronous distributed system using multi threading. There are n+1
threads in the system: Each of the n processes will be simulated by one thread and there is one master thread. The
master thread will “inform” all threads when one round starts. Thus, each thread simulating one process, before it can
begin round x, must wait for the master thread for a "go ahead" signal for round x. Clearly, the master thread can give
the signal to start round x to the threads only if the master thread is sure that all the n threads (simulating n
processes) have completed their previous round (round x-1).

Your simulation will simulate the FloodMax algorithm for leader election with termination detection built in. The code (
algorithm) executed by all processes must be the same.

The input for this problem is to be read from a file inputdata.txt, which is a text file and it consists of
(1) n (the number of processes of the distributed system which is equal to the number of threads to be created), (2) one
array uid[n] of size n with array elements separated by one or more space(s); the i th element of this array gives the
unique identifier of the i th process or i th thread and all ids are integers, and (3) n rows of adjacency information.
The jth row of the adjacency information lists the indices of all processes that the jth process is connected to. Each
row has 1 or more indices (indicating its neighbors) separated by white spaces.

### Tasks

- Flood Max Algo : Spencer, Arjun, Anirudh
- Config Reader : Anirudh
- Driver Class for spinning up N Threads : Spencer, Anirudh
- ServiceBus : Arjun
- Process class: Arjun, Spencer, Anirudh

### Project Structure

```
utd-dc-projects1
    +-- src
        |-- IAmDonePayload.java                         (IAM Done Payload)
        |-- RejectPayload.java                          (Reject Payload)
        |-- TerminatePayload.java                       (Termination Payload, containing leaderId)
        |-- SearchPayload.java                          (Search Payload, with maxId so far)
        |-- FloodMaxLeaderElectionManager.java          (Code for spinning up N Threads)
        |-- FloodMaxLeaderElectionSyncProcess.java      (FloodMax Leader Election algorithm)
        |-- GlobalConstants.java                        (Used for setting Log Level)
        |-- LogLevel.java                               (LogLevel enum)
        |-- ConfigFileReader.java                       (Config file reader)
        |-- Message.java                                (Message class wraps payload)
        |-- Listener.java                               (Inteface with onReceive())
        |-- SharedMemoryBus.java                        (Communication Singleton class) 
        |-- ProcessId.java                              (may have information to break ties)
        |-- SyncProcess.java                            (abstract Process class which can be extended by algo classes)
        |-- DCException.java                            (Exception wrapper)
        |-- TreeNode.java                               (Used to maintain BFS tree links)
        |-- TimeUtils.java                              (Utility class for time)
        |-- Driver.java                                 (Main class)
    |-- README.md
```

### Sample Input File Content (Different from the one provided)

```text
8
12 18 58 56 55 54 13 60
2 5 6 7
1 3 4 6 7
2 4
2 3 5 6
1 4 6
1 2 4 5 7
1 2 6 8
7
```

### Compile Section

```shell
cd src/
javac Driver.java
java Driver [textFilePath]
```

### Working

The code base contains 3 main sections.

#### 1. Sync Process:

Abstracts out all the round related functionalities into this class, following `Template Pattern`. Contains `send()`
, `receive()`, `nextRound()` etc. The `algorithm process` upon extending this will implement, `preprocess()`
, `incoming()`, `outgoing()` functions. The process executes `nextRound()` function util `terminate` flag is true.
The `enableNextRound()` acts as a toggle switch: when it is true, we can start a new round, and then go in the `wait()`
stage with resetting the value back to false. When the `enableNextRound()` gets invoked, we set the flag as true and
call the `resume()` to start the while loop again and go to the next round.

#### 2. Service Bus:

Singleton class that handles all communication edges in the network. Uses adjacency list. When
a `send(src, dest, message)` is invoke, it fetches the Destination Process from the Map and calls the `onRecieve()`
function of the Listener Process.

#### 3. Flood Max Algorithm:

Flood Max with Termination detection (using BFS Tree). Algorithm executes in rounds. In the first round, every process
sends its leaderId to every other neighbour. In the second round, the process will work on the messages received in the
previous round, ie round one, and performs the appropriate actions (includes, updating leader, sending IAM done, sending
reject etc.). Then it sends, the message to the outgoing neighbour based on the current process state (ie leaderId).

##### Concurrency

Inorder to avoid concurrency modification issues, we are maintaining two lists: `currRoundReceivedMessages`
& `prevRoundReceivedMessages`. All the messages received in the current round, will be added
to `currRoundReceivedMessages` and after completing the round, it will be added to the `prevRoundReceivedMessages` (it
will be flushed before adding). This ensures that the list, `prevRoundReceivedMessages` is not modified when doing
current round processing.

##### Reject Message

When you get `reject` message from all the neighbours, you declare yourself as a leaf node, and send `IAmDoneMessage` to
the parent.

##### IAM Done Message

When you get a `IAmDone` message from a neighbour, you associate that neighbour as a child in your `bfs tree`. When you
get `IAmDone` from all your neighbours, you send `IAmDone` to your parent.

##### Parent Node

When you get `IAmDone` from all your neighbours, your parent is null. You declare yourself as a leader. You send
terminate message.

##### Terminate

When the leader is elected, we send the leaderId to all the node. This message also sets the isTerminate flag to true,
forcing the Process while loop to exit and there by closing the thread. When all the threads are closed, we end the main
process (this check is performed in the ElectionManager). 
