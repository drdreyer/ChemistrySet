// An implementation of the classic Treiber stack via reagents

package chemistry

import scala.annotation.tailrec
import java.util.concurrent.atomic._

final class TreiberStack[A] {

/*
  val push: Reagent[A,Unit] = head.fastUpd[A,Unit](
    { (xs,x) => x::xs },
    { (_, _) => () }
  )

  val tryPop: Reagent[Unit,Option[A]] = head.fastUpd[Unit,Option[A]](
    { case (x::xs, _) => xs
      case (emp,   _) => emp },
    { case (x::xs, _) => Some(x)
      case (emp,   _) => None }
  )
*/
/*
  private val head = Ref[List[A]](List())

  val push: Reagent[A,Unit] = head.updIn { 
    (xs,x) => x::xs
  }

  val tryPop: Reagent[Unit,Option[A]] = head.upd[Option[A]] {
    case (x::xs) => (xs,  Some(x))
    case emp     => (emp, None)
  }

  val pop: Reagent[Unit,A] = head.upd[A] {
    case (x::xs) => (xs, x)
  }
*/

  private val headX = new AtomicReference[List[A]](List())

  object push extends Reagent[A,Unit] {
    def tryReact(x:A, rx: Reaction): Any = {
      val xs = headX.get
      if (headX.compareAndSet(xs,x::xs)) () else ShouldRetry
    }
    def makeOfferI(x:A, offer: Offer[Unit]) {}
    def composeI[B](next: Reagent[Unit,B]) = throw Util.Impossible
    def maySync = false
    def alwaysCommits = false
  }

  object tryPop extends Reagent[Unit,Option[A]] {
    def tryReact(u:Unit, rx: Reaction): Any = headX.get match {
      case (ov@(x::xs)) => 
	if (headX.compareAndSet(ov,xs)) Some(x) else ShouldRetry
      case emp     => None
    }
    def makeOfferI(u:Unit, offer: Offer[Option[A]]) {}
    def composeI[B](next: Reagent[Option[A],B]) = throw Util.Impossible
    def maySync = false
    def alwaysCommits = false
  }

  object pop extends Reagent[Unit,A] {
    @tailrec def tryReact(u:Unit, rx: Reaction): Any = headX.get match {
      case (ov@(x::xs)) => 
	if (headX.compareAndSet(ov,xs)) x else ShouldRetry
      case emp     => tryReact(u, rx)
    }
    def makeOfferI(u:Unit, offer: Offer[A]) {}
    def composeI[B](next: Reagent[A,B]) = throw Util.Impossible
    def maySync = false
    def alwaysCommits = false
  }

/*
  private val headX = Ref[List[A]](List())

  val dpush: Reagent[A,Int] = head.upd { 
    (xs,x) => (x::xs, 0)
  }

  val dtryPop: Reagent[Unit,Int] = head.upd[Int] {
    case (x::xs) => (xs,  0)
    case emp     => (emp, 0)
  }
*/ 
}
