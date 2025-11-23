package knu.atoz.post.exception;

public abstract class PostException extends RuntimeException {
  public PostException(String message) {
    super(message);
  }
}
