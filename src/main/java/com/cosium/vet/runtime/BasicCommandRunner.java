package com.cosium.vet.runtime;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created on 16/02/18.
 *
 * @author Reda.Housni-Alaoui
 */
public class BasicCommandRunner implements CommandRunner {

  private static final Logger LOG = LoggerFactory.getLogger(BasicCommandRunner.class);

  @Override
  public String run(Path workingDir, String... command) {
    try {
      ProcessBuilder processBuilder =
          new ProcessBuilder(command)
              .directory(workingDir.toFile())
              .redirectInput(ProcessBuilder.Redirect.INHERIT)
              .redirectError(ProcessBuilder.Redirect.INHERIT);

      Process process = processBuilder.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new CommandRunException(
            exitCode, IOUtils.toString(process.getInputStream(), "UTF-8"), command);
      }

      return IOUtils.toString(process.getInputStream(), "UTF-8").trim();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
