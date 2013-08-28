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
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.rules.TemporaryFolder;

import Chisel._


/** This testsuite checks the generation of dot graphs.
*/
class MultiClockSuite extends AssertionsForJUnit {

  val tmpdir = new TemporaryFolder();

  @Before def initialize() {
    tmpdir.create()
  }

  @After def done() {
    tmpdir.delete()
  }

  def assertFile( filename: String, content: String ) {
    val source = scala.io.Source.fromFile(filename, "utf-8")
    val lines = source.mkString
    source.close()
    assert(lines === content)
  }

  /** Test Register on a different clock */
  @Test def testRegClock() {

    class ClockedSubComp extends Module {
      val io = new Bundle {
        val ready = Bool(INPUT)
        val valid = Bool(OUTPUT)
      }
      val stored = Reg(next=io.ready, clock=new Clock())
      io.valid := stored
    }

    class Comp extends Module {
      val io = new Bundle {
        val data0 = Bool(INPUT)
        val data1 = Bool(INPUT)
        val result = Bool(OUTPUT)
      }
      val sub = Module(new ClockedSubComp())
      sub.io.ready := io.data0 & io.data1
      io.result := sub.io.valid
    }

    chiselMain(Array[String]("--v",
      "--targetDir", tmpdir.getRoot().toString()),
      () => Module(new Comp()))
    assertFile(tmpdir.getRoot() + "/MultiClockSuite_Comp_1.v",
"""module MultiClockSuite_ClockedSubComp_1(input T0,
    input  io_ready,
    output io_valid
);

  reg[0:0] stored;

  assign io_valid = stored;

  always @(posedge T0) begin
    stored <= io_ready;
  end
endmodule

module MultiClockSuite_Comp_1(input T0,
    input  io_data0,
    input  io_data1,
    output io_result
);

  wire T0;
  wire sub_io_valid;

  assign T0 = io_data0 & io_data1;
  assign io_result = sub_io_valid;
  MultiClockSuite_ClockedSubComp_1 sub(.T0(T0),
       .io_ready( T0 ),
       .io_valid( sub_io_valid )
  );
endmodule

""")
  }
}
