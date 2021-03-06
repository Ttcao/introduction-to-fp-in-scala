package intro

//import com.sun.javaws.exceptions.InvalidArgumentException

import scala.util.{Failure, Success, Try}

/*
 * Handling errors without exceptions....
 * ======================================
 */

/*
 * A well-typed set of errors that can occur.
 */
sealed trait Error
case class NotANumber(s: String) extends Error
case class InvalidOperation(s: String) extends Error
case class UnexpectedInput(s: String) extends Error
case object NotEnoughInput extends Error

/*
 * A result type that represents one of our errors or a success.
 */
case class Fail[A](error: Error) extends Result[A]
case class Ok[A](value: A) extends Result[A]

sealed trait Result[A] {
  /*
   * Exercise 1:
   *
   * We often want to work with data structures by breaking them
   * down by cases. With lists, this operation is foldRight. For
   * our result type this is just called fold. More formally we
   * refer to this as a catamorphism. Implement fold for Result.
   *
   * Hint: Try using pattern matching.
   *
   * scala> Ok(1).fold(_ => 0, x => x)
   *  = 1
   *
   * scala> Fail[Int](NotEnoughInput).fold(_ => 0, x => x)
   *  = 0
   */
  def fold[X](fail: Error => X, ok: A => X): X =
    this match {
      case Fail(error) => fail(error)
      case Ok(a) => ok(a)
    }

  /*
   * Exercise 2:
   *
   * Implement flatMap (a.k.a. bind, a.k.a. >>=).
   *
   * The following law must hold:
   *   r.flatMap(f).flatMap(g) == r.flatMap(z => f(z).flatMap(g))
   *
   * scala> Ok(1).flatMap(x => Ok(x + 10))
   *  = Ok(11)
   *+/
   * scala> Ok(1).flatMap(x => Fail[Int](NotEnoughInput))
   *  = Fail(NotEnoughInput)
   *
   * scala> Fail[Int](NotEnoughInput).flatMap(x => Ok(x + 10))
   *  = Fail(NotEnoughInput)
   *
   * scala> Fail[Int](NotEnoughInput).flatMap(x => Fail[Int](UnexpectedInput("?")))
   *  = Fail(NotEnoughInput)
   *
   * Advanced: Try using fold.
   */
  def flatMap[B](f: A => Result[B]): Result[B] =
    this match {
      case Fail(error) => Fail(error)
      case Ok(a) => f(a)
    }

  /*
   * Exercise 3:
   *
   * Implement map for Result[A].
   *
   * The following laws must hold:
   *  1) r.map(z => z) == r
   *  2) r.map(z => f(g(z))) == r.map(g).map(f)
   *
   * scala> Ok(1).map(x => x + 10)
   *  = Ok(11)
   *
   * scala> Fail[Int](NotEnoughInput).map(x => x + 10)
   *  = Fail(NotEnoughInput)
   *
   * Advanced: Try using flatMap.
   */
  def map[B](f: A => B): Result[B] =
    this match {
      case Fail(error) => Fail(error)
      case Ok(a) => Ok(f(a))
    }

  /*
   * Exercise 4:
   *
   * Extract the value if it is success case otherwise use default value.
   *
   * scala> Ok(1).getOrElse(10)
   *  = 1
   *
   * scala> Fail(NotEnoughInput).getOrElse(10)
   *  = 10
   */
  def getOrElse(otherwise: => A): A =
    this match {
      case Fail(_) => otherwise
      case Ok(a) => a
    }

  /*
   * Exercise 5:
   *
   * Implement choice, take this result if successful otherwise take
   * the alternative.
   *
   * scala> Ok(1) ||| Ok(10)
   *  = Ok(1)
   *
   * scala> Ok(1) ||| Fail[Int](NotEnoughInput)
   *  = Ok(1)
   *
   * scala> Fail[Int](NotEnoughInput) ||| Ok(10)
   *  = Ok(10)
   *
   * scala> Fail[Int](NotEnoughInput) ||| Fail[Int](UnexpectedInput("?"))
   *  = Fail[Int](UnexpectedInput("?"))
   */
  def |||(alternative: => Result[A]): Result[A] =
    this match {
      case Fail(_) => alternative
      case Ok(a) => Ok(a)
    }
}

object Result {
  def notANumber[A](s: String): Result[A] =
    fail(NotANumber(s))

  def unexpectedInput[A](s: String): Result[A] =
    fail(UnexpectedInput(s))

  def notEnoughInput[A]: Result[A] =
    fail(NotEnoughInput)

  def ok[A](value: A): Result[A] =
    Ok(value)

  def fail[A](error: Error): Result[A] =
    Fail(error)

  /*
   * *Challenge* Exercise 6:
   *
   * Sequence a list of Result into an Result of Lists by producing
   * Ok of a list of all the values or returning Fail on the first
   * Fail case.
   *
   * scala> Lists.sequence(List[Result[Int]](Ok(1), Ok(2), Ok(3)))
   * resX: Result[List[Int]] = Ok(List(1, 2, 3))
   *
   * scala> Lists.sequence(List[Result[Int]](Ok(1), Fail(NotEnoughInput), Ok(3)))
   * resX: Result[List[Int]] = Fail(NotEnoughInput)
   */
  def sequence[A](xs: List[Result[A]]): Result[List[A]] =
    ???
}


/*
 * *Challenge* Exercise 7: The worlds most trivial calculator.
 *
 * We are implementing a way to compute a number on the command line.
 *  - The first argument is the operation, that is one of +, - or *
 *  - The second argument is an integer, n
 *  - The third argument is an integer, m
 *
 * Complete the implementation, some of the methods are provided
 * with type signatures to get started.
 */
object ResultExample {

  /** Simplified calculation data type. */
  sealed trait Operation
  case object Plus extends Operation
  case object Minus extends Operation
  case object Multiply extends Operation

  /*
   * Parse an int if it is valid, otherwise fail with NotAnInt.
   *
   * Hint: Scala defines String#toInt, but warning it throws exceptions
   *       if it is not a valid Int :| i.e. use try catch.
   */
  def int(body: String): Result[Int] =
    try {
      val n = body.toInt
      Ok(n)
    } catch {
      case e: NumberFormatException => Result.notANumber(e.getMessage)
//      case e: InvalidArgumentException => Result.notANumber(e.getMessage)
      case e: Exception => Result.notANumber(e.getMessage)
    }

//  def int2(body: String): Result[Int] =
//    Try(body.toInt) match {
//      case Success(n) => Ok(n)
//      case Failure(err: NumberFormatException) => Result.notANumber(err.getMessage)
//      case Failure(err: InvalidArgumentException) => Result.notANumber(err.getMessage)
//      case Failure(err) => Result.notANumber(err.getMessage)
//    }

  /*
   * Parse the operation if it is valid, otherwise fail with InvalidOperation.
   */
  def operation(op: String): Result[Operation] =
    op match {
      case "Plus" => Ok(Plus)
      case "Minus" => Ok(Minus)
      case "Multiply" => Ok(Multiply)
      case _ => Fail(InvalidOperation("Invalid operation"))
    }

  /*
   * Compute an `answer`, by running operation for n and m.
   */
  def calculate(op: Operation, n: Int, m: Int): Int =
    op match {
      case Plus => n + m
      case Minus => n - m
      case Multiply => n * m
      case _ => 0
    }

  /*
   * Attempt to compute an `answer`, by:
   *  - parsing operation
   *  - parsing n
   *  - parsing m
   *  - running calculation
   *
   * hint: use flatMap / map
   */
  def attempt(op: String, n: String, m: String): Result[Int] =
    for {
      op <- operation(op)
      x <- Ok(n.toInt)
      y <- Ok(m.toInt)
    } yield calculate(op, x, y)

  /*
   * Run a calculation by pattern matching three elements off the input arguments,
   * parsing the operation, a value for n and a value for m.
   */
  def run(args: List[String]): Result[Int] =
    ???

  def main(args: Array[String]) =
    println(run(args.toList) match {
      case Ok(result) => s"result: ${result}"
      case Fail(error) => s"failed: ${error}"
    })
}
