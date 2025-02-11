package scalaz
package std

import scala.annotation.tailrec

sealed trait OptionInstances0 extends OptionInstances1 {
  implicit def optionEqual[A](implicit A0: Equal[A]): Equal[Option[A]] = new OptionEqual[A] {
    implicit def A = A0
  }

  implicit def optionSemiLattice[A: SemiLattice]: SemiLattice[Option[A]] =
    new OptionSemigroup[A] with SemiLattice[Option[A]] {
      override def B = implicitly
    }
}

sealed trait OptionInstances1 {
  implicit def optionBand[A: Band]: Band[Option[A]] =
    new OptionSemigroup[A] with Band[Option[A]] {
      override def B = implicitly
    }
}

trait OptionInstances extends OptionInstances0 {
  implicit val optionInstance: Traverse[Option] with MonadPlus[Option] with Alt[Option] with BindRec[Option] with Cozip[Option] with Zip[Option] with Unzip[Option] with Align[Option] with IsEmpty[Option] with Cobind[Option] with Optional[Option] =
    new Traverse[Option] with MonadPlus[Option] with Alt[Option] with BindRec[Option] with Cozip[Option] with Zip[Option] with Unzip[Option] with Align[Option] with IsEmpty[Option] with Cobind[Option] with Optional[Option] {
      def point[A](a: => A) = Some(a)
      override def index[A](fa: Option[A], n: Int) = if (n == 0) fa else None
      override def length[A](fa: Option[A]) = if (fa.isEmpty) 0 else 1
      override def ap[A, B](fa: => Option[A])(f: => Option[A => B]) = f match {
        case Some(f) => fa match {
          case Some(x) => Some(f(x))
          case None    => None
        }
        case None    => None
      }
      def bind[A, B](fa: Option[A])(f: A => Option[B]) = fa flatMap f
      override def map[A, B](fa: Option[A])(f: A => B) = fa map f
      def traverseImpl[F[_], A, B](fa: Option[A])(f: A => F[B])(implicit F: Applicative[F]) =
        fa map (a => F.map(f(a))(Some(_): Option[B])) getOrElse F.point(None)
      def empty[A]: Option[A] = None
      def plus[A](a: Option[A], b: => Option[A]) = a orElse b
      override def alt[A](a: => Option[A], b: => Option[A]): Option[A] =
        plus(a, b)
      override def foldRight[A, B](fa: Option[A], z: => B)(f: (A, => B) => B) = fa match {
        case Some(a) => f(a, z)
        case None    => z
      }
      def cozip[A, B](a: Option[A \/ B]) =
        a match {
          case None => -\/(None)
          case Some(e) => e match {
            case -\/(a) => -\/(Some(a))
            case \/-(b) => \/-(Some(b))
          }
        }
      def zip[A, B](a: => Option[A], b: => Option[B]) =
        for {
          x <- a
          y <- b
        } yield (x, y)
      def unzip[A, B](a: Option[(A, B)]) =
        a match {
          case None => (None, None)
          case Some((a, b)) => (Some(a), Some(b))
        }

      def alignWith[A, B, C](f: A \&/ B => C) = {
        case (None, None) =>
          None
        case (Some(a), None) =>
          Some(f(\&/.This(a)))
        case (None, Some(b)) =>
          Some(f(\&/.That(b)))
        case (Some(a), Some(b)) =>
          Some(f(\&/.Both(a, b)))
      }

      def cobind[A, B](fa: Option[A])(f: Option[A] => B) =
        fa map (a => f(Some(a)))

      override def cojoin[A](a: Option[A]) =
        a map (Some(_))

      def pextract[B, A](fa: Option[A]): Option[B] \/ A =
        fa match {
          case Some(a) => \/-(a)
          case None    => -\/(None)
        }
      override def isDefined[A](fa: Option[A]): Boolean = fa.isDefined
      override def toOption[A](fa: Option[A]): Option[A] = fa
      override def getOrElse[A](o: Option[A])(d: => A) = o getOrElse d

      @scala.annotation.tailrec
      def tailrecM[A, B](a: A)(f: A => Option[A \/ B]): Option[B] =
        f(a) match {
          case None => None
          case Some(-\/(a)) => tailrecM(a)(f)
          case Some(\/-(b)) => Some(b)
        }

      override def unfoldrPsumOpt[S, A](seed: S)(f: S => Maybe[(Option[A], S)]): Maybe[Option[A]] = {
        @tailrec def go(s1: S): Option[A] = f(s1) match {
          case Maybe.Just((ma, s2)) => ma match {
            case a @ Some(_) =>
              a
            case _ =>
              go(s2)
          }
          case _ =>
            None
        }
        f(seed).map {
          case (a @ Some(_), _) =>
            a
          case (_, s) =>
            go(s)
        }
      }

      override def unfoldrOpt[S, A, B](seed: S)(f: S => Maybe[(Option[A], S)])(implicit r: Reducer[A, B]): Maybe[Option[B]] = {
        @tailrec def go(acc: B, s1: S): Option[B] = f(s1) match {
          case Maybe.Just((ma, s2)) => ma match {
            case Some(a) =>
              go(r.snoc(acc, a), s2)
            case _ =>
              None
          }
          case _ =>
            Some(acc)
        }
        f(seed).map {
          case (Some(a), s) =>
            go(r.unit(a), s)
          case _ =>
            None
        }
      }
    }

  implicit def optionMonoid[A: Semigroup]: Monoid[Option[A]] =
    new OptionSemigroup[A] with Monoid[Option[A]] {
      override def B = implicitly
      override def zero = None
    }

  /** Add `None` as an element less than all `A`s. */
  implicit def optionOrder[A](implicit A0: Order[A]): Order[Option[A]] = new OptionOrder[A] {
    implicit def A = A0
  }

  implicit def optionShow[A: Show]: Show[Option[A]] = {
    import scalaz.syntax.show._
    Show.show {
      case Some(a1) => cord"Some($a1)"
      case None     => Cord("None")
    }
  }

  implicit def optionFirst[A]: Monoid[FirstOption[A]] with Band[FirstOption[A]] = new Monoid[FirstOption[A]] with Band[FirstOption[A]] {
    def zero: FirstOption[A] = Tag(None)

    def append(f1: FirstOption[A], f2: => FirstOption[A]) = Tag(Tag.unwrap(f1).orElse(Tag.unwrap(f2)))
  }

  implicit def optionFirstShow[A: Show]: Show[FirstOption[A]] = Tag.subst(Show[Option[A]])

  implicit def optionFirstOrder[A: Order]: Order[FirstOption[A]] = Tag.subst(Order[Option[A]])

  implicit def optionFirstMonad: Monad[FirstOption] = Tags.First.subst1[Monad, Option](Monad[Option])

  implicit def optionLast[A]: Monoid[LastOption[A]] with Band[LastOption[A]] = new Monoid[LastOption[A]] with Band[LastOption[A]] {
    def zero: LastOption[A] = Tag(None)

    def append(f1: LastOption[A], f2: => LastOption[A]) = Tag(Tag.unwrap(f2).orElse(Tag.unwrap(f1)))
  }

  implicit def optionLastShow[A: Show]: Show[LastOption[A]] = Tag.subst(Show[Option[A]])

  implicit def optionLastOrder[A: Order]: Order[LastOption[A]] = Tag.subst(Order[Option[A]])

  implicit def optionLastMonad: Monad[LastOption] = Tags.Last.subst1[Monad, Option](Monad[Option])

  implicit def optionMin[A](implicit o: Order[A]): Monoid[MinOption[A]] with Band[MinOption[A]] = new Monoid[MinOption[A]] with Band[MinOption[A]] {
    def zero: MinOption[A] = Tag(None)

    def append(f1: MinOption[A], f2: => MinOption[A]) = Tag( (Tag unwrap f1, Tag unwrap f2) match {
      case (Some(v1), Some(v2)) => Some(Order[A].min(v1, v2))
      case (_f1 @ Some(_), None) => _f1
      case (None, _f2 @ Some(_)) => _f2
      case (None, None) => None
    })
  }

  implicit def optionMinShow[A: Show]: Show[MinOption[A]] = Tag.subst(Show[Option[A]])

  implicit def optionMinOrder[A: Order]: Order[MinOption[A]] = Tag.subst(Order[Option[A]])

  implicit def optionMinMonad: Monad[MinOption] = Tags.Min.subst1[Monad, Option](Monad[Option])

  implicit def optionMax[A](implicit o: Order[A]): Monoid[MaxOption[A]] with Band[MaxOption[A]] = new Monoid[MaxOption[A]] with Band[MaxOption[A]] {
    def zero: MaxOption[A] = Tag(None)

    def append(f1: MaxOption[A], f2: => MaxOption[A]) = Tag( (Tag unwrap f1, Tag unwrap f2) match {
      case (Some(v1), Some(v2)) => Some(Order[A].max(v1, v2))
      case (_f1 @ Some(_), None) => _f1
      case (None, _f2 @ Some(_)) => _f2
      case (None, None) => None
    })
  }

  implicit def optionMaxShow[A: Show]: Show[MaxOption[A]] = Tag.subst(Show[Option[A]])

  implicit def optionMaxOrder[A: Order]: Order[MaxOption[A]] = Tag.subst(Order[Option[A]])

  implicit def optionMaxMonad: Monad[MaxOption] = Tags.Max.subst1[Monad, Option](Monad[Option])
}

trait OptionFunctions {
  /** [[scala.Some.apply]] with a sometimes more convenient type. */
  final def some[A](a: A): Option[A] = Some(a)

  /** [[scala.None]] with a sometimes more convenient type. */
  final def none[A]: Option[A] = None

  /**
   * Catamorphism over the option. Returns the provided function `some` applied to item contained in the Option
   * if it is defined, otherwise, the provided value `none`.
   */
  final def cata[A, X](oa: Option[A])(some: A => X, none: => X): X = oa match {
    case None    => none
    case Some(a) => some(a)
  }

  /**Alias for `cata` */
  final def fold[A, X](oa: Option[A])(some: A => X, none: => X): X = cata(oa)(some, none)

  final def toSuccess[A, E](oa: Option[A])(e: => E): Validation[E, A] = oa match {
    case Some(a) => Success(a)
    case None    => Failure(e)
  }

  final def toFailure[A, B](oa: Option[A])(b: => B): Validation[A, B] = oa match {
    case Some(e) => Failure(e)
    case None    => Success(b)
  }

  final def toSuccessNel[A,E](oa: Option[A])(e: => E) : ValidationNel[E,A] = oa match {
    case Some(a) => Success(a)
    case None    => Failure(NonEmptyList(e))
  }

  final def toFailureNel[A,B](oa: Option[A])(b: => B) : ValidationNel[A,B] = oa match {
    case Some(a) => Failure(NonEmptyList(a))
    case None    => Success(b)
  }

  final def toRight[A, E](oa: Option[A])(e: => E): E \/ A = oa match {
    case Some(a) => \/-(a)
    case None    => -\/(e)
  }

  final def toLeft[A, B](oa: Option[A])(b: => B): A \/ B = oa match {
    case Some(a) => -\/(a)
    case None    => \/-(b)
  }

  final def toMaybe[A](oa: Option[A]): Maybe[A] = oa match {
    case Some(a) => Maybe.just(a)
    case None    => Maybe.empty
  }

  /**
   * Returns the item contained in the Option wrapped in type M if the Option is defined,
   * otherwise, the empty value for type M.
   */
  final def orEmpty[A, M[_] : Applicative : PlusEmpty](oa: Option[A]): M[A] = oa match {
    case Some(a) => Applicative[M].point(a)
    case None    => PlusEmpty[M].empty
  }

  /**
   * Returns the item contained in the Option wrapped in type F if the Option is defined,
   * otherwise, returns the supplied action.
   */
  final def getOrElseF[A, F[_] : Applicative](oa: Option[A])(fa: => F[A]): F[A] = oa match {
    case Some(a) => Applicative[F].point(a)
    case None    => fa
  }

  /**
   * Returns the given value if None, otherwise lifts the Some value and passes it to the given function.
   */
  final def foldLift[F[_], A, B](oa: Option[A])(b: => B, k: F[A] => B)(implicit p: Applicative[F]): B = oa match {
    case None    => b
    case Some(a) => k(Applicative[F].point(a))
  }

  /**
   * Returns the given value if None, otherwise lifts the Some value to Option and passes it to the given function.
   */
  final def foldLiftOpt[A, B](oa: Option[A])(b: => B, k: Option[A] => B): B = {
    import scalaz.std.option.optionInstance
    foldLift[Option, A, B](oa)(b, k)
  }
}

object option extends OptionInstances with OptionFunctions {
  object optionSyntax extends scalaz.syntax.std.ToOptionOps with scalaz.syntax.std.ToOptionIdOps
}

//
// Type class implementation traits
//

private trait OptionEqual[A] extends Equal[Option[A]] {
  implicit def A: Equal[A]

  override def equalIsNatural: Boolean = A.equalIsNatural

  override def equal(o1: Option[A], o2: Option[A]): Boolean = (o1, o2) match {
    case (Some(a1), Some(a2)) => A.equal(a1, a2)
    case (None, None)         => true
    case (None, Some(_))      => false
    case (Some(_), None)      => false
  }
}


private trait OptionOrder[A] extends Order[Option[A]] with OptionEqual[A] {
  implicit def A: Order[A]

  import Ordering._

  def order(f1: Option[A], f2: Option[A]) = (f1, f2) match {
    case (Some(a1), Some(a2)) => Order[A].order(a1, a2)
    case (None, Some(_))      => LT
    case (Some(_), None)      => GT
    case (None, None)         => EQ
  }
}

private trait OptionSemigroup[A] extends Semigroup[Option[A]] {
  def B: Semigroup[A]

  def append(a: Option[A], b: => Option[A]): Option[A] = (a, b) match {
    case (Some(aa), Some(bb)) => Some(B.append(aa, bb))
    case (Some(_), None) => a
    case (None, b2 @ Some(_)) => b2
    case (None, None) => None
  }
}
