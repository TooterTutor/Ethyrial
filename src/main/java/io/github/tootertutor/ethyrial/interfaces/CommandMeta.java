package io.github.tootertutor.ethyrial.interfaces;

/** Optional: attach defaults without hard-coding in classes. */
public @interface CommandMeta {
    String name(); // required canonical name

    String[] aliases() default {};

    String usage() default ""; // e.g. "/ethyrial give <item> [amount] [player]"

    String description() default "";
}
