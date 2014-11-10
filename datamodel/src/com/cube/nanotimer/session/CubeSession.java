package com.cube.nanotimer.session;

import java.util.LinkedList;
import java.util.List;

public class CubeSession extends CubeBaseSession {

  public static final int SESSION_MAX_SIZE = 13; // 12, + 1 for deletion

  public CubeSession() {
    super();
  }

  public CubeSession(List<Long> sessionTimes) {
    super(sessionTimes);
  }

  public long getRAOfFive() {
    return getRAOf(5);
  }

  public long getRAOfTwelve() {
    return getRAOf(12);
  }

  public void addTime(long time) {
    sessionTimes.addFirst(time);
    if (sessionTimes.size() > SESSION_MAX_SIZE) {
      sessionTimes.removeLast();
    }
  }

  public void setLastAsDNF() {
    if (!sessionTimes.isEmpty()) {
      sessionTimes.set(0, (long) -1);
    }
  }

  public void setLastAsPlusTwo() {
    if (!sessionTimes.isEmpty()) {
      long curLastTime = sessionTimes.get(0);
      if (curLastTime > 0) {
        sessionTimes.set(0, curLastTime + 2000);
      }
    }
  }

  public void deleteLast() {
    if (!sessionTimes.isEmpty()) {
      sessionTimes.removeFirst();
    }
  }

  public void clearSession() {
    if (sessionTimes != null) {
      sessionTimes.clear();
    }
  }

  public List<Long> getSessionTimes() {
    if (sessionTimes.size() > 12) {
      return sessionTimes.subList(0, 12);
    } else {
      return sessionTimes;
    }
  }

  public void setSessionTimes(LinkedList<Long> sessionTimes) {
    this.sessionTimes = sessionTimes;
  }

}
