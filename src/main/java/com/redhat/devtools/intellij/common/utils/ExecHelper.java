/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.common.utils;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.TerminalOptionsProvider;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.redhat.devtools.intellij.common.CommonConstants.HOME_FOLDER;

public class ExecHelper {
  private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

  public static void executeAfter(Runnable runnable, long delay, TimeUnit unit) {
    SERVICE.schedule(runnable, delay, unit);
  }

  public static void submit(Runnable runnable) {
    SERVICE.submit(runnable);
  }

  public static String execute(String executable, boolean checkExitCode, File workingDirectory, String... arguments) throws IOException {
    DefaultExecutor executor = new DefaultExecutor() {
      @Override
      public boolean isFailure(int exitValue) {
        if (checkExitCode) {
          return super.isFailure(exitValue);
        } else {
          return false;
        }
      }
    };
    StringWriter writer = new StringWriter();
    PumpStreamHandler handler = new PumpStreamHandler(new WriterOutputStream(writer));
    executor.setStreamHandler(handler);
    executor.setWorkingDirectory(workingDirectory);
    CommandLine command = new CommandLine(executable).addArguments(arguments);
    try {
      executor.execute(command);
      return writer.toString();
    } catch (IOException e) {
      throw new IOException(e.getLocalizedMessage() + " " + writer.toString(), e);
    }
  }

  public static String execute(String executable, String... arguments) throws IOException {
    return execute(executable, true, new File(HOME_FOLDER), arguments);
  }

  public static String execute(String executable, File workingDirectory, String... arguments) throws IOException {
    return execute(executable, true, workingDirectory, arguments);
  }

  public static String execute(String executable, boolean checkExitCode, String... arguments) throws IOException {
    return execute(executable, checkExitCode, new File(HOME_FOLDER), arguments);
  }

  private static class RedirectedStream extends FilterInputStream {
    private boolean emitLF = false;
    private final boolean redirect;
    private final boolean delay;

    private RedirectedStream(InputStream delegate, boolean redirect, boolean delay) {
      super(delegate);
      this.redirect = redirect;
      this.delay = delay;
    }

    @Override
    public synchronized int read() throws IOException {
      if (emitLF) {
        emitLF = false;
        return '\n';
      } else {
        int c = super.read();
        if (redirect && c == '\n') {
          emitLF = true;
          c = '\r';
        }
        return c;
      }
    }

    @Override
    public synchronized int read(@NotNull byte[] b) throws IOException {
      return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(@NotNull byte[] b, int off, int len) throws IOException {
      if (b == null) {
        throw new NullPointerException();
      } else if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return 0;
      }

      int c = read();
      if (c == -1) {
        if (delay) {
          try {
            Thread.sleep(60000L);
          } catch (InterruptedException e) {}
        }
        return -1;
      }
      b[off] = (byte)c;

      int i = 1;
      try {
        for (; i < len  && available() > 0; i++) {
          c = read();
          if (c == -1) {
            break;
          }
          b[off + i] = (byte)c;
        }
      } catch (IOException ee) {}
      return i;
    }
  }
  private static class RedirectedProcess extends Process {
    private final Process delegate;
    private final InputStream inputStream;

    private RedirectedProcess(Process delegate, boolean redirect, boolean delay) {
      this.delegate = delegate;
      inputStream = new RedirectedStream(delegate.getInputStream(), redirect, delay) {};
    }

    @Override
    public OutputStream getOutputStream() {
      return delegate.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
      return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
      return delegate.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
      return delegate.waitFor();
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
      return delegate.waitFor(timeout, unit);
    }

    @Override
    public int exitValue() {
      return delegate.exitValue();
    }

    @Override
    public void destroy() {
      delegate.destroy();
    }

    @Override
    public Process destroyForcibly() {
      return delegate.destroyForcibly();
    }

    @Override
    public boolean isAlive() {
      return delegate.isAlive();
    }
  }

  private static void executeWithTerminalInternal(File workingDirectory , String... command) throws IOException {
    try {
      ProcessBuilder builder = new ProcessBuilder(command).directory(workingDirectory).redirectErrorStream(true);
      Process p = builder.start();
      boolean isPost2018_3 = ApplicationInfo.getInstance().getBuild().getBaselineVersion() >= 183;
      p = new RedirectedProcess(p, true, isPost2018_3);

      final Process process = p;
      AbstractTerminalRunner runner = new AbstractTerminalRunner(ProjectManager.getInstance().getDefaultProject()) {
        @Override
        protected Process createProcess(@Nullable String s) {
          return process;
        }

        @Override
        protected ProcessHandler createProcessHandler(Process process) {
          return null;
        }

        @Override
        protected String getTerminalConnectionName(Process process) {
          return null;
        }

        @Override
        protected TtyConnector createTtyConnector(Process process) {
          return new ProcessTtyConnector(process, StandardCharsets.UTF_8) {
            @Override
            protected void resizeImmediately() {
            }

            @Override
            public String getName() {
              return "Odo";
            }

            @Override
            public boolean isConnected() {
              return process.isAlive();
            }
          };
        }

        @Override
        public String runningTargetName() {
          return null;
        }
      };
      TerminalOptionsProvider terminalOptions = ServiceManager.getService(TerminalOptionsProvider.class);
      terminalOptions.setCloseSessionOnLogout(false);
      final TerminalView view = TerminalView.getInstance(ProjectManager.getInstance().getOpenProjects()[0]);
      final Method[] method = new Method[1];
      final Object[][] parameters = new Object[1][];
      try {
        method[0] = TerminalView.class.getMethod("createNewSession", new Class[] {Project.class, AbstractTerminalRunner.class});
        parameters[0] = new Object[] {ProjectManager.getInstance().getOpenProjects()[0],
                runner};
      } catch (NoSuchMethodException e) {
        try {
          method[0] = TerminalView.class.getMethod("createNewSession", new Class[] {AbstractTerminalRunner.class});
          parameters[0] = new Object[] { runner};
        } catch (NoSuchMethodException e1) {
          throw new IOException(e1);
        }
      }
      ApplicationManager.getApplication().invokeLater(() -> {
        try {
          method[0].invoke(view, parameters[0]);
        } catch (IllegalAccessException|InvocationTargetException e) {}
      });
      if (p.waitFor() != 0) {
        throw new IOException("Process returned exit code: " + p.exitValue(), null);
      }
    } catch (IOException e) {
      throw e;
    }
    catch (InterruptedException e) {
      throw new IOException(e);
    }
  }

  private static void executeWithTerminalInternal(Project project, String... command) {
    TerminalView terminalView = TerminalView.getInstance(project);
    LocalTerminalDirectRunner runner = new LocalTerminalDirectRunner(project) {

      @Override
      protected TtyConnector createTtyConnector(PtyProcess process) {
        return new ProcessTtyConnector(process, StandardCharsets.UTF_8) {
          @Override
          protected void resizeImmediately() {
          }

          @Override
          public String getName() {
            return "Tekton";
          }

          @Override
          public boolean isConnected() {
            return process.isAlive();
          }
        };
      }

      @Override
      public String runningTargetName() {
        return null;
      }

      @Override
      protected PtyProcess createProcess(@Nullable String directory) throws ExecutionException {
        Map<String, String> envs = Collections.emptyMap();
        try {
          return PtyProcess.exec(command, envs, HOME_FOLDER);
        } catch (IOException e) {
          throw new ExecutionException(e);
        }
      }
    };
    terminalView.createNewSession(project, runner);
  }

  public static void executeWithTerminal(File workingDirectory, String... command) throws IOException {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      execute(command[0], workingDirectory, Arrays.stream(command)
              .skip(1)
              .toArray(String[]::new));
    } else {
      executeWithTerminalInternal(workingDirectory, command);
    }
  }

  public static void executeWithTerminal(String... command) throws IOException {
    executeWithTerminal(new File(HOME_FOLDER), command);
  }

  public static void executeWithTerminal(Project project, String... command) {
    executeWithTerminalInternal(project, command);
  }
}