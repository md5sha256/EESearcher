package me.andrewandy.eesearcher;

@FunctionalInterface
public interface Authenticator {

    boolean tryAuth(final String user, final String password);

}
