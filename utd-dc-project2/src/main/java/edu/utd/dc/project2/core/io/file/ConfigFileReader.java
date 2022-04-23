package edu.utd.dc.project2.core.io.file;

import edu.utd.dc.project2.constants.GlobalConstants;
import edu.utd.dc.project2.constants.LogLevel;
import edu.utd.dc.project2.core.support.ProcessId;
import edu.utd.dc.project2.exceptions.DCException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

/** Contains Parser logic for reading config file and creating adjList etc from the content. */
public class ConfigFileReader {

  private final Map<Integer, List<Integer>> adjList;

  private List<ProcessId> processIdList;
  private int size;

  public ConfigFileReader(String configFileName) {

    adjList = new LinkedHashMap<>();

    parseFile(configFileName);
  }

  private void parseFile(String configFileName) {
    try (BufferedReader br = new BufferedReader(new FileReader(configFileName))) {
      int lineCounter = 1;
      String line = br.readLine();

      while (line != null) {
        if (!line.isEmpty() && Character.isDigit(line.charAt(0))) {

          log(LogLevel.TRACE, "Reading line " + line);

          if (lineCounter == 1) {
            size = Integer.parseInt(line);
          } else if (lineCounter == 2) {

            String[] tokens = line.split("#")[0].split(" ");

            processIdList =
                Arrays.stream(tokens)
                    .mapToInt(Integer::parseInt)
                    .mapToObj(ProcessId::new)
                    .collect(Collectors.toList());

          } else if (lineCounter >= 3 && lineCounter <= 2 + size) {

            String[] tokens = line.split("#")[0].split(" ");

            Integer keyIdx = (lineCounter - 1 - 2);
            adjList.putIfAbsent(keyIdx, new ArrayList<>());

            adjList
                .get(keyIdx)
                .addAll(
                    Arrays.stream(tokens)
                        .mapToInt(Integer::parseInt)
                        .map(e -> e - 1)
                        .boxed()
                        .collect(Collectors.toList()));

          } else {
            break;
          }

          lineCounter++;
        }
        line = br.readLine();
      }
    } catch (Exception ex) {
      throw new DCException("Error parsing config", ex);
    }
  }

  public int getSize() {
    return size;
  }

  public Map<Integer, List<Integer>> getAdjList() {
    return adjList;
  }

  public List<ProcessId> getProcessIdList() {
    return processIdList;
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("Size: ").append(getSize()).append("\n");
    getAdjList()
        .forEach(
            (k, v) -> {
              sb.append(k).append(" -- ");
              for (Integer processId : v) sb.append(processId).append(", ");
              sb.append("\n");
            });
    return sb.toString();
  }

  private void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
