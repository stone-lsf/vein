package com.sm.finance.charge.server.discovery;

/**
 * @author shifeng.luo
 * @version created on 2017/9/2 下午1:32
 */
public enum LifeState {
    init {
        @Override
        public LifeState onStart(LifecycleComponent component) {
            component.onStarting();
            return starting;
        }

        @Override
        public LifeState onClose(LifecycleComponent component) {
            component.onClosing();
            return closing;
        }
    },

    starting {
        @Override
        public LifeState onStart(LifecycleComponent component) {
            return this;
        }

        @Override
        public LifeState onStartComplete(LifecycleComponent component) {
            component.onStarted();
            return started;
        }

        @Override
        public LifeState onClose(LifecycleComponent component) {
            component.onClosing();
            return closing;
        }
    },

    started {
        @Override
        public LifeState onStartComplete(LifecycleComponent component) {
            return this;
        }

        @Override
        public LifeState onStop(LifecycleComponent component) {
            component.onStopping();
            return stopping;
        }
    },

    stopping {
        @Override
        public LifeState onStop(LifecycleComponent component) {
            return this;
        }

        @Override
        public LifeState onStopComplete(LifecycleComponent component) {
            component.onStopped();
            return stopped;
        }

        @Override
        public LifeState onClose(LifecycleComponent component) {
            component.onClosing();
            return closing;
        }
    },

    stopped {
        @Override
        public LifeState onStart(LifecycleComponent component) {
            component.onStarting();
            return starting;
        }

        @Override
        public LifeState onStopComplete(LifecycleComponent component) {
            return this;
        }

        @Override
        public LifeState onClose(LifecycleComponent component) {
            component.onClosing();
            return closing;
        }
    },

    closing {
        @Override
        public LifeState onClose(LifecycleComponent component) {
            return this;
        }

        @Override
        public LifeState onCloseComplete(LifecycleComponent component) {
            component.onClosed();
            return closing;
        }
    },

    closed {
        @Override
        public LifeState onCloseComplete(LifecycleComponent component) {
            return this;
        }
    };

    public LifeState onStart(LifecycleComponent component) {
        throw new IllegalStateException("current state:" + this + " can't move to starting state");
    }

    public LifeState onStartComplete(LifecycleComponent component) {
        throw new IllegalStateException("current state:" + this + " can't move to started state");
    }

    public LifeState onStop(LifecycleComponent component) {
        throw new IllegalStateException("current state:" + this + " can't move to stopping state");
    }

    public LifeState onStopComplete(LifecycleComponent component) {
        throw new IllegalStateException("current state:" + this + " can't move to stopped state");
    }

    public LifeState onClose(LifecycleComponent component) {
        throw new IllegalStateException("current state:" + this + " can't move to closing state");
    }

    public LifeState onCloseComplete(LifecycleComponent component) {
        throw new IllegalStateException("current state:" + this + " can't move to closed state");
    }
}
