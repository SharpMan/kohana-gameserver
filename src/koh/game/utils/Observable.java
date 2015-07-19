package koh.game.utils;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Alleos13
 */
public abstract class Observable<O extends Observer> {

    private volatile boolean changed;
    public CopyOnWriteArrayList<O> observers;
    //private ReentrantLock notifyLock = new ReentrantLock();

    public Observable() {
        observers = new CopyOnWriteArrayList<O>();
    }

    public void Observable$setChanged() {
        this.changed = true;
    }

    public boolean Observable$hasChanged() {
        return changed;
    }

    public void Observable$unsetChanged() {
        changed = false;
    }

    public void Observable$register(O obs) {
        observers.addIfAbsent(obs);
    }

    public void Observable$unregister(O obs) {
        observers.remove(obs);
    }

    public void Observable$notify() {
        /*
         * final ReentrantLock lock = notifyLock; lock.lock();
         */
        try {
            if (!changed) {
                return;
            }
            for (Observer o : observers) {
                o.Observer$update(this);
            }
        } finally {
            //lock.unlock();
        }
    }

    public void Observable$notify(Object arg) {
        //final ReentrantLock lock = notifyLock;
        //lock.lock();
        try {
            if (!changed) {
                return;
            }
            for (Observer o : observers) {
                o.Observer$update(this, arg);
            }
        } finally {
            // lock.unlock();
        }
    }

    public void Observable$notify(Object... args) {
        //final ReentrantLock lock = notifyLock;
        //lock.lock();
        try {
            if (!changed) {
                return;
            }
            for (Observer o : observers) {
                o.Observer$update(this, args);
            }
        } finally {
            //lock.unlock();
        }
    }

    public int Observable$size() {
        return observers.size();
    }

    public void Observable$reset() {
        //final ReentrantLock lock = notifyLock;
        //lock.lock();
        try {
            for (Observer o : observers) {
                o.Observer$reset(this);
            }
            observers.clear();
        } finally {
            // lock.unlock();
        }
    }
}
