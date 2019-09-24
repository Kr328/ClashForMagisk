package com.github.kr328.clash.runner;

import com.github.kr328.clash.ClashConfigure;
import com.github.kr328.clash.StarterConfigure;

import java.util.concurrent.LinkedBlockingQueue;

public class ClashManager implements ClashProcess.Callback {
    private ClashProcess process;

    private String dataDir;
    private String baseDir;

    private LinkedBlockingQueue<Command> commandList = new LinkedBlockingQueue<>();
    private Command currentCommand;

    public ClashManager(String baseDir, String dataDir) {
        this.baseDir = baseDir;
        this.dataDir = dataDir;
        this.process = new ClashProcess(this);
    }

    @Override
    public synchronized void onStatusChanged(ClashProcess.Status status) {
        if ( currentCommand == null )
            return;

        switch (status) {
            case STARTING:
                currentCommand.onStarting();
                break;
            case STARTED:
                currentCommand.onStarted();
                break;
            case KILLING:
                currentCommand.onKilling();
                break;
            case STOPPED:
                currentCommand.onStopped();
                break;
        }
    }

    public void exec() throws InterruptedException {
        while ( !Thread.currentThread().isInterrupted() ) {
            synchronized (this) {
                currentCommand = commandList.take();
            }

            currentCommand.exec();

            synchronized (this) {
                currentCommand = null;
            }
        }
    }

    private abstract class Command {
        abstract void exec() throws InterruptedException;
        void onStopped() {};
        void onStarted() {};
        void onKilling() {};
        void onStarting() {};
    }

    private class StartCommand extends Command {
        @Override
        public void exec() throws InterruptedException {
            if ( process.getStatus() != ClashProcess.Status.STOPPED )
                return;

            process.start(baseDir, dataDir);

            synchronized (this) {
                if ( process.getStatus() == ClashProcess.Status.STARTING)
                    this.wait();
            }
        }

        @Override
        public void onStarted() {
            synchronized (this) {
                this.notify();
            }
        }
    }

    private class StopCommand extends Command {
        @Override
        void exec() throws InterruptedException {
            if ( process.getStatus() != ClashProcess.Status.STARTED )
                return;

            process.kill();

            synchronized (this) {
                if ( process.getStatus() == ClashProcess.Status.KILLING )
                    this.wait();
            }
        }

        @Override
        void onStopped() {
            synchronized (this) {
                this.notify();
            }
        }
    }

    private class ReloadCommand extends Command {
        @Override
        void exec() throws InterruptedException {

        }
    }

}
