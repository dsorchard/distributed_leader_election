package edu.utd.dc.project2.core.io.file;

import edu.utd.dc.project2.constants.GlobalConstants;
import edu.utd.dc.project2.constants.LogLevel;
import edu.utd.dc.project2.core.support.ProcessId;
import edu.utd.dc.project2.exceptions.DCException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Contains Parser logic for reading config file and creating adjList etc from the content. */
public class ConfigFileReader {

  private final Map<Integer, List<Integer>> adjList;

  private List<ProcessId> processIdList;
  private int size;

  private int root;

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

            processIdList =
                IntStream.range(0, size).mapToObj(ProcessId::new).collect(Collectors.toList());

          } else if (lineCounter == 2) {
            root = Integer.parseInt(line) - 1;
          } else if (lineCounter > 2 && lineCounter <= 2 + size) {

            String[] tokens = line.split("#")[0].split(" ");

            Integer keyIdx = (lineCounter - 3);
            adjList.putIfAbsent(keyIdx, new ArrayList<>());

            for (int i = 0; i < tokens.length; i++) {
              if (keyIdx == i) continue;
              if (tokens[i].equals("1")) adjList.get(keyIdx).add(i);
            }

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

  public int getRoot() {
    return root;
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("Size: ").append(getSize()).append("\n");
    sb.append("Root: ").append(getRoot()).append("\n");

    sb.append("ProcessId List: ");
    getProcessIdList().forEach(sb::append);
    sb.append("\n");

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
