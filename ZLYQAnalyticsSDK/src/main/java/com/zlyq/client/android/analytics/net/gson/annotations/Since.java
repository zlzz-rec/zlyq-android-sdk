package com.zlyq.client.android.analytics.net.gson.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates the version number since a member or a type has been present.
 * This annotation is useful to manage versioning of your Json classes for a web-service.
 *
 * <p>
 * This annotation has no effect unless you build {@link com.google.gson.Gson} with a
 * {@link com.google.gson.GsonBuilder} and invoke
 * {@link com.google.gson.GsonBuilder#setVersion(double)} method.
 *
 * <p>Here is an example of how this annotation is meant to be used:</p>
 * <pre>
 * public class User {
 *   private String firstName;
 *   private String lastName;
 *   &#64Since(1.0) private String emailAddress;
 *   &#64Since(1.0) private String password;
 *   &#64Since(1.1) private Address address;
 * }
 * </pre>
 *
 * <p>If you created EGson with {@code new EGson()}, the {@code toJson()} and {@code fromJson()}
 * methods will use all the fields for serialization and deserialization. However, if you created
 * EGson with {@code EGson gson = new GsonBuilder().setVersion(1.0).create()} then the
 * {@code toJson()} and {@code fromJson()} methods of EGson will exclude the {@code address} field
 * since it's version number is set to {@code 1.1}.</p>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Since {
  /**
   * the value indicating a version number since this member
   * or type has been present.
   */
  double value();
}
