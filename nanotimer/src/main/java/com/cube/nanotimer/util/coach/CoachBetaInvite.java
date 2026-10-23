package com.cube.nanotimer.util.coach;

/**
 * The invitation to apply for the coach beta, for builds on the internal testing track.
 *
 * <p>While it is on, the smart-cube news sheet leads with the coach's free offer and opens by itself
 * the first time the app starts, and the smart-cube sheet carries a way back to it. <b>It has to be
 * false in a production build</b>: the form is for a beta that ends, and the offer is not one made
 * to every user.
 */
public final class CoachBetaInvite {

  public static final boolean ENABLED = true;

  /** The coach beta's application form. */
  public static final String APPLY_URL =
      "https://docs.google.com/forms/d/e/1FAIpQLSd0A286e1hA4FvL8JF8Sr4fZECKoK2CXu-5-ua0gRK2fxqlVA/viewform";

  private CoachBetaInvite() {
  }
}
