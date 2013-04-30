/*
 Copyright (c) 2011, 2012, 2013 The Regents of the University of
 California (Regents). All Rights Reserved.  Redistribution and use in
 source and binary forms, with or without modification, are permitted
 provided that the following conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the following
      two paragraphs of disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      two paragraphs of disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Regents nor the names of its contributors
      may be used to endorse or promote products derived from this
      software without specific prior written permission.

 IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF
 ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION
 TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 MODIFICATIONS.
*/

import scala.collection.mutable.ArrayBuffer
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.rules.TemporaryFolder;

import Chisel._

/** This testsuite checks the primitives of the standard library
  that will generate basic common graphs of *Node*.
*/
class StdlibSuite extends AssertionsForJUnit {

  val tmpdir = new TemporaryFolder();

  @Before def initialize() {
    tmpdir.create()
  }

  @After def done() {
    tmpdir.delete()
  }

  /** test of simple operators */
  @Test def testOperators() {

    class OperatorComp extends Component {
      val io = new Bundle {
        val x = UFix(INPUT, 8)
        val y = UFix(INPUT, 8)
        val ys = Fix(INPUT, 8)
        val z = UFix(OUTPUT)
        val zb = Bool(OUTPUT)
        val zs = Fix(OUTPUT)
      }

      // apply(bit: Int): UFix
      val a = io.x(0)

      // apply(hi: Int, lo: Int): UFix
      val b = io.x(4, 3)
      val c = io.x(3, 4)
      val d = io.x(3, 3)
      val e = io.x(9, -1)

      // apply(bit: UFix): UFix
      val a1 = io.x(UFix(0))
      // apply(hi: UFix, lo: UFix): UFix
      val b1 = io.x(UFix(4), UFix(3))
      val c1 = io.x(UFix(3), UFix(4))
      val d1 = io.x(UFix(3), UFix(3))
      val e1 = io.x(UFix(9), UFix(-1))

      // apply(range: (Int, Int)): UFix
      val f = io.x((5, 3))

      // unary_-(): UFix
      val g = - io.x

      // unary_~(): UFix
      val h = ~io.x

      // andR(): Bool
      val i = io.x.andR

      // orR():  Bool
      val j = io.x.orR

      // xorR():  Bool
      val k = io.x.xorR

      // << (b: UFix): UFix
      val l = io.x << a

      // >> (b: UFix): UFix
      val m = io.x >> a

      // +  (b: UFix): UFix
      val n = io.x + io.y

      // *  (b: UFix): UFix
      val o = io.x * io.y

      // /  (b: UFix): UFix
      val p = io.x / io.y

      // %  (b: UFix): UFix
      val q = io.x % io.y

      // ^  (b: UFix): UFix
      val r = io.x ^ io.y

      // ?  (b: UFix): UFix
      val s = io.x ? io.y

      // -  (b: UFix): UFix
      val t = io.x - io.y

      // ## (b: UFix): UFix
      val u = io.x ## io.y

      // &  (b: UFix): UFix
      val ab = io.x & io.y

      // |  (b: UFix): UFix
      val ac = io.x | io.y

      io.z := (a | b | c | d
        | a1 | b1 | c1 | d1
        | f | g | h | i | j | k
        | l | m | n | o | p | q
        | r | u | ab | ac
        /* XXX Computing any of those signals throws an exception */
        /* | e | t | e1 | s */
      ).toUFix

      // -- result type is Bool --

      // ===(b: UFix): Bool
      val v = io.x === io.y

      // != (b: UFix): Bool
      val w = io.x != io.y

      // >  (b: UFix): Bool
      val x = io.x > io.y

      // <  (b: UFix): Bool
      val y = io.x < io.y

      // <= (b: UFix): Bool
      val z = io.x <= io.y

      // >= (b: UFix): Bool
      val aa = io.x >= io.y

      io.zb := (v | w | x | y | z | aa)

      // -- result type is Fix --

      // *   (b: Fix): Fix
      val ad = io.x * io.ys

      // %   (b: Fix): Fix
      val ae = io.x % io.ys

      // /   (b: Fix): Fix
      val af = io.x / io.ys

      io.zs := (ad | ae | af)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new OperatorComp())
  }

  /** Concatenate two nodes X and Y in a node Z such that
    Z[0..wx+wy] = X[0..wx] :: Y[0..wy]. */
  @Test def testCat() {

    class CatComp extends Component {
      val io = new Bundle {
        val x = UFix(INPUT, 8)
        val y = UFix(INPUT, 8)
        val z = UFix(OUTPUT)
      }
      io.z := Cat(io.x, io.y)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new CatComp())
  }

  /** Generate a lookup into an array.
    XXX Lookup.scala, use different code based on instance of CppBackend. */
  @Test def testLookup() {

    class LookupComp extends Component {
      val io = new Bundle {
        val addr = Bits(INPUT, 8)
        val data = UFix(OUTPUT)
      }
      io.data := Lookup(io.addr, UFix(0), Array(
        (Bits(0), UFix(10)),
        (Bits(1), UFix(11))))
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new LookupComp())
  }

  /** Generate a PopCount
    */
  @Test def testPopCount() {

    class PopCountComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = UFix(OUTPUT)
      }
      io.out := PopCount(Array(Bool(true), Bool(false)))
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new PopCountComp())
  }

  /** Generate a Reverse
    */
  @Test def testReverse() {

    class ReverseComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := Reverse(io.in)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new ReverseComp())
  }

  /** Generate a ShiftRegister
    */
  @Test def testShiftRegister() {

    class ShiftRegisterComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := ShiftRegister(2, io.in)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new ShiftRegisterComp())
  }

  /** Generate a UFixToOH
    */
  @Test def testUFixToOH() {

    class UFixToOHComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out0 = UFix(OUTPUT)
        val out1 = UFix(OUTPUT)
      }
      io.out0 := UFixToOH(io.in)
      io.out1 := UFixToOH(io.in, 4)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new UFixToOHComp())
  }

  /** Generate a foldR
    */
  @Test def testfoldR() {

    class foldRComp extends Component {
      val io = new Bundle {
        val in0 = UFix(INPUT, 8)
        val in1 = UFix(INPUT, 8)
        val out = UFix(OUTPUT)
      }
      io.out := foldR(io.in0 :: io.in1 :: Nil){ _ + _ }
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new foldRComp())
  }

  /** Generate a ArbiterCtrl
    */
  @Test def testArbiterCtrl() {

    class ArbiterCtrlComp extends Component {
      val io = new Bundle {
        val in0 = Bool(INPUT)
        val in1 = Bool(INPUT)
        val out = Bool(OUTPUT)
      }
      val x = ArbiterCtrl(io.in0 :: io.in1 :: Nil)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new ArbiterCtrlComp())
  }

  /** Generate a FillInterleaved
    */
  @Test def testFillInterleaved() {

    class FillInterleavedComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := FillInterleaved(4, io.in)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new FillInterleavedComp())
  }

  /** Generate a Counter
    */
  @Test def testCounter() {

    class CounterComp extends Component {
      val io = new Bundle {
        val in = Bool(INPUT)
        val out = UFix(OUTPUT)
        val wrap = Bool(OUTPUT)
      }
      val (count, wrap) = Counter(io.in, 5)
      io.out := count
      io.wrap := wrap
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new CounterComp())
  }

  /** Generate a PriorityMux
    */
  @Test def testPriorityMux() {

    class PriorityMuxComp extends Component {
      val io = new Bundle {
        val in0 = Bits(INPUT, 1)
        val in1 = Bits(INPUT, 1)
        val data0 = Bits(INPUT, 16)
        val data1 = Bits(INPUT, 16)
        val out0 = Bits(OUTPUT)
        val out1 = Bits(OUTPUT)
        val out2 = Bits(OUTPUT)
      }
      io.out0 := PriorityMux((io.in0, io.data0) :: (io.in1, io.data1) :: Nil)
      io.out1 := PriorityMux(io.in0 :: io.in1 :: Nil,
        io.data0 :: io.data1 :: Nil)
      io.out2 := PriorityMux(io.in0, io.data0 :: io.data1 :: Nil)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new PriorityMuxComp())
  }

  /** Generate a PriorityEncoder
    */
  @Test def testPriorityEncoder() {

    class PriorityEncoderComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = UFix(OUTPUT)
      }
      io.out := PriorityEncoder(io.in)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new PriorityEncoderComp())
  }

  /** Generate a PriorityEncoderOH
    */
  @Test def testPriorityEncoderOH() {

    class PriorityEncoderOHComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := PriorityEncoderOH(io.in)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new PriorityEncoderOHComp())
  }

  /** Generate a Fill
    */
  @Test def testFill() {

    class FillComp extends Component {
      val io = new Bundle {
        val in = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := Fill(4, io.in)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new FillComp())
  }

  /** Generate a Log2
    */
  @Test def testLog2() {

    class Log2Comp extends Component {
      val io = new Bundle {
        val in = UFix(INPUT, 8)
        val out = UFix(OUTPUT)
      }
      io.out := Log2(io.in, 2)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new Log2Comp())
  }

  /** Generate a MuxLookup
    */
  @Test def testMuxLookup() {

    class MuxLookupComp extends Component {
      val io = new Bundle {
        val key = Bits(INPUT, 8)
        val in0 = Bits(INPUT, 8)
        val in1 = Bits(INPUT, 8)
        val default = Bits(INPUT, 16)
        val data0 = Bits(INPUT, 16)
        val data1 = Bits(INPUT, 16)
        val out = Bits(OUTPUT)
      }
      io.out := MuxLookup(io.key, io.default,
        (io.in0, io.data0) :: (io.in1, io.data1) :: Nil)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new MuxLookupComp())
  }

  /** Generate a MuxCase
    */
  @Test def testMuxCase() {

    class MuxCaseComp extends Component {
      val io = new Bundle {
        val default = Bits(INPUT, 8)
        val in0 = Bits(INPUT, 8)
        val in1 = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := MuxCase(io.default,
        (Bool(true), io.in0) :: (Bool(false), io.in1) :: Nil)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new MuxCaseComp())
  }

  /** Generate a Multiplex
    */
  @Test def testMultiplex() {

    class MultiplexComp extends Component {
      val io = new Bundle {
        val t = Bits(INPUT, 1)
        val c = Bits(INPUT, 8)
        val a = Bits(INPUT, 8)
        val out = Bits(OUTPUT, 8)
      }
      // XXX Cannot figure out which code to write. Multiplex returns a Node.
      // val x = Multiplex(io.t, io.c, io.a)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new MultiplexComp())
  }

  /** Generate a Mux
    */
  @Test def testMux() {

    class MuxComp extends Component {
      val io = new Bundle {
        val t = Bool(INPUT)
        val c = Bits(INPUT, 8)
        val a = Bits(INPUT, 8)
        val out = Bits(OUTPUT)
      }
      io.out := Mux(io.t, io.c, io.a)
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => new MuxComp())
  }

}
