# Distributed Computing Projects
Collection of all Distributed Computing Projects done as a part of `UTD 6380 : Distributed Computing`

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

- ~~Flood Max Algo~~
- ~~Config Reader~~
- ~~Driver Class for spinning up N Threads~~
- ~~Process Class with send(), receive() etc.~~
- ~~Evaluate `Singleton` vs `Queue` message passing layer: Queue is required.~~
- `Node with Algo` vs `Algo with Node` node structure

### Project Structure
```
utd-dc-projects1
    +-- src/edu/utd/dc/project0
         +-- algo
            +-- leaderelection.floodmax
                +-- domain.payload
                    |-- IAmDonePayload.java                         (IAM Done Payload)
                    |-- RejectPayload.java                          (Reject Payload)
                    |-- TerminatePayload.java                       (Termination Payload, containing leaderId)
                    |-- SearchPayload.java                          (Search Payload, with maxId so far)
                |-- FloodMaxLeaderElectionManager.java              (Code for spinning up N Threads)
                |-- FloodMaxLeaderElectionSyncProcess.java          (FloodMax Leader Election algorithm)
         +-- constants
            |-- GlobalConstants.java                                (Used for setting Log Level)
            |-- LogLevel.java                                       (LogLevel enum)
         +-- core
            +-- io
                +-- file
                    |-- ConfigFileReader.java                       (Config file reader)
                +-- sharedmemory                            
                    +-- domain
                        |-- Message.java                            (Message class wraps payload)
                    |-- Listener.java                               (Inteface with onReceive())
                    |-- SharedMemoryBus.java                        (Communication Singleton class) 
            +-- support
                |-- ProcessId.java                                  (may have information to break ties)
            |-- SyncProcess.java                                    (abstract Process class which can be extended by algo classes)
         +-- exceptions
            |-- DCException.java                                    (Exception wrapper)
         +-- tree
            |-- TreeNode.java                                       (Used to maintain BFS tree links)
         +-- utils
            |-- TimeUtils.java                                      (Utility class for time)
         |-- Driver.java                                            (Main class)
    |-- README.md
```
### Sample Input File Content
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