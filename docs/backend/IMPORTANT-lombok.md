# Project Lombok in Dashoboards
_What's that annotation do?_

Starting with the most common:

## `@UtilityClass`
Used to generate pure-static classes. This automatically marks all members `static`, the class as `final`
(non-extendable). You should use this on Utility classes (hence the name) and on Route classes (which must be static for
Autorouter).

## `@Slf4j`
Generates a `log` Logger object for you! It's automatically created with best practices in mind, so just make use of the
provided `log` object.

## `@Data`
Automates the chores of data/POJO classes. Auto-generates field getters, `toString()`, `equals()`, `hashcode()` etc. If
you have a simple class that only exists to hold data, use this and `@RequiredArgsConstructor` on the class, and let
Lombok generate your accessors/constructor. Make sure all your fields are _final_ or they won't be included in the
generated constructor!

## `@RequiredArgsConstructor`
Usually used in conjunction with `@Data`, this generates a constructor for you from all `final` member fields.
