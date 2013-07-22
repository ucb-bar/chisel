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

package Chisel
import scala.collection.mutable.ArrayBuffer
import scala.math._
import java.io.File;
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import scala.sys.process._
import Node._
import Reg._
import ChiselError._
import Literal._
import scala.collection.mutable.HashSet

class FloBackend extends Backend {
  val keywords = new HashSet[String]();

  override def emitDec(node: Node): String =
    emitRef(node) + " = "

  override def emitTmp(node: Node): String =
    emitRef(node)

  override def emitRef(node: Node): String = {
    if (node.litOf == null) {
      node match {
        case x: Literal =>
          "" + x.value

        case x: Binding =>
          emitRef(x.inputs(0))

        case x: Bits =>
          if (!node.isInObject && node.inputs.length == 1) emitRef(node.inputs(0)) else super.emitRef(node)

        case _ =>
          super.emitRef(node)
      }
    } else {
      "" + node.litOf.value
    }
  }

  def emit(node: Node): String = {
    node match {
      case x: Mux =>
        emitDec(x) + "mux " + emitRef(x.inputs(0)) + " " + emitRef(x.inputs(1)) + " " + emitRef(x.inputs(2)) + "\n"

      case o: Op =>
        emitDec(o) +
        (if (o.inputs.length == 1) {
          o.op match {
            case "~" => "not " + emitRef(node.inputs(0))
            case "-" => "neg " + emitRef(node.inputs(0))
            case "!" => "not " + emitRef(node.inputs(0))
          }
         } else {
           o.op match {
             case "<"  => "lt/"  + node.inputs(0).width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "<=" => "gt/"  + node.inputs(0).width + " " + emitRef(node.inputs(1)) + " " + emitRef(node.inputs(0))
             case ">"  => "gt/"  + node.inputs(0).width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case ">=" => "lt/"  + node.inputs(0).width + " " + emitRef(node.inputs(1)) + " " + emitRef(node.inputs(0))
             case "+"  => "add/" + node.width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "-"  => "sub/" + node.width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "*"  => "mul/" + node.width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "!"  => "not/" + node.width + " " + emitRef(node.inputs(0))
             case "<<" => "lsh/" + node.width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case ">>" => "rsh/" + node.width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "##" => "cat/" + node.inputs(1).width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "|"  => "or "  + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "||" => "or "  + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "&"  => "and " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "&&" => "and " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "^"  => "xor " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "==" => "eq "  + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
             case "!=" => "neq " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1))
           }
         }) + "\n"

      case x: Extract =>
        emitDec(node) + "rsh/" + node.width + " " + emitRef(node.inputs(0)) + " " + emitRef(node.inputs(1)) + "\n"

      case x: Fill =>
        emitDec(x) + "fill/" + node.width + " " + emitRef(node.inputs(0)) + "\n"

      case x: Bits =>
        if( x.inputs.length == 1 ) {
          emitDec(x) + "mov " + emitRef(x.inputs(0)) + "\n"
        } else {
          emitDec(x) + "rnd/" + x.width + "\n"
        }
      case m: Mem[_] =>
        emitDec(m) + "mem " + m.n + "\n"

      case m: MemRead =>
        emitDec(m) + "ld " + emitRef(m.mem) + " " + emitRef(m.addr) + "\n" // emitRef(m.mem)

      case m: MemWrite =>
        if (m.inputs.length == 2) {
          return ""
        }
        emitDec(m) + "st " + emitRef(m.mem) + " " + emitRef(m.addr) + " " + emitRef(m.data) + "\n"

      case x: Reg => // TODO: need resetVal treatment
        emitDec(x) + "reg " + emitRef(x.updateVal) + "\n"

      case x: Log2 => // TODO: log2 instruction?
        emitDec(x) + "log2/" + x.width + " " + emitRef(x.inputs(0)) + "\n"

      case _ =>
        ""
    }
  }

  def renameNodes(c: Module, nodes: Seq[Node]) = {
    for (m <- nodes) {
      m match {
        case l: Literal => ;
        case any        =>
          if (m.name != "" && !(m == c.reset) && !(m.component == null)) {
            // only modify name if it is not the reset signal or not in top component
            if(m.name != "reset" || !(m.component == c)) {
              m.name = m.component.getPathName + "__" + m.name;
            }
          }
      }
    }
  }

  override def elaborate(c: Module): Unit = {
    super.elaborate(c)

    for (cc <- Module.components) {
      if (!(cc == c)) {
        c.mods       ++= cc.mods;
        c.blackboxes ++= cc.blackboxes;
        c.debugs     ++= cc.debugs;
      }
    }
    c.findConsumers();
    c.verifyAllMuxes;
    ChiselError.checkpoint()

    c.collectNodes(c);
    c.findOrdering(); // search from roots  -- create omods
    renameNodes(c, c.omods);
    if (Module.isReportDims) {
      val (numNodes, maxWidth, maxDepth) = c.findGraphDims();
      ChiselError.info("NUM " + numNodes + " MAX-WIDTH " + maxWidth + " MAX-DEPTH " + maxDepth);
    }

    // Write the generated code to the output file
    val out = createOutputFile(c.name + ".flo");
    for (m <- c.omods)
      out.write(emit(m));
    out.close();
  }

}
