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
import Node._
import Reg._
import ChiselError._
import scala.reflect._

object Reg {

  def regMaxWidth(m: Node) =
    if (isInGetWidth) {
      throw new Exception("getWidth was called on a Register or on an object connected in some way to a Register that has a statically uninferrable width")
    } else {
      maxWidth(m)
    }

  // Rule: If no width is specified, use max width. Otherwise, use the specified width.
  def regWidth(w: Int) =
    if(w <= 0) {
      regMaxWidth _ ;
    } else {
      fixWidth(w)
    }

  /** Rule: if r is using an inferred width, then don't enforce a width. If it is using a user inferred
    width, set the the width

    XXX Can't specify return type. There is a conflict. It is either
    (Node) => (Int) or Int depending which execution path you believe.
    */
  def regWidth(r: Node) = {
    val rLit = r.litOf
    if (rLit != null && rLit.hasInferredWidth) {
      regMaxWidth _
    } else {
      fixWidth(r.getWidth)
    }
  }

  def validateGen[T <: Data](gen: => T) {
    for ((n, i) <- gen.flatten)
      if (!i.inputs.isEmpty || !i.updates.isEmpty) {
        throwException("Invalid Type Specifier for Reg")
      }
  }

  /** *out* defines the data type of the register when it is read.
    *updateVal* and *resetVal* define the update and reset values
    respectively.
    */
  def apply[T <: Data](out: T, updateVal: T, resetVal: T): T = {
    val gen = out.clone
    validateGen(gen)

    val d: Array[(String, Bits)] =
      if(updateVal == null) {
        gen.flatten.map{case(x, y) => (x -> null)}
      } else {
        updateVal.flatten
      }

    // asOutput flip the direction and returns this.
    val res = gen.asOutput

    if(resetVal != null) {
      for((((res_n, res_i), (data_n, data_i)), (rval_n, rval_i)) <- res.flatten zip d zip resetVal.flatten) {

        assert(rval_i.getWidth > 0,
          {ChiselError.error("Negative width to wire " + res_i)})
        val reg = new Reg()

        reg.init("", regWidth(rval_i), data_i, rval_i)

        // make output
        reg.isReset = true
        res_i.inputs += reg
        res_i.comp = reg
      }
    } else {
      for(((res_n, res_i), (data_n, data_i)) <- res.flatten zip d) {
        val w = res_i.getWidth
        val reg = new Reg()
        reg.init("", regWidth(w), data_i)

        // make output
        res_i.inputs += reg
        res_i.comp = reg
      }
    }
    res.setIsTypeNode
    res
  }


  def apply[T <: Data](gen: T): T = {
    Reg[T](gen, null.asInstanceOf[T], null.asInstanceOf[T])
  }
}

object RegUpdate {

  def apply[T <: Data](updateVal: T): T = Reg[T](updateVal, updateVal, null.asInstanceOf[T])

}


object RegReset {

  def apply[T <: Data](resetVal: T): T = Reg[T](resetVal, null.asInstanceOf[T], resetVal)

}

class Reg extends Delay with proc {
  def updateVal: Node = inputs(0);
  def resetVal: Node  = inputs(1);
  def enableSignal: Node = inputs(enableIndex);
  var enableIndex = 0;
  var hasResetSignal = false
  var isReset = false
  var isEnable = false;
  def isUpdate: Boolean = !(updateVal == null);
  def update (x: Node) { inputs(0) = x };
  var assigned = false;
  var enable = Bool(false);

  def procAssign(src: Node) {
    if (assigned) {
      ChiselError.error("reassignment to Reg");
    }
    val cond = genCond();
    if (conds.length >= 1) {
      isEnable = Mod.backend.isInstanceOf[VerilogBackend]
      enable = enable || cond;
    }
    updates += ((cond, src))
  }
  override def genMuxes(default: Node): Unit = {
    if(isMemOutput) {
      inputs(0) = updates(0)._2
      return
    }
    if(isEnable){
      // hack to force the muxes to match the Reg's width:
      // the intent is u = updates.head._2
      val u = new Mux().init("", maxWidth _, Bool(true), updates.head._2, this)
      u.component = this.component
      genMuxes(u, updates.toList.tail)
      inputs += enable;
      enableIndex = inputs.length - 1;
    } else {
      super.genMuxes(default)
    }
  }
  def nameOpt: String = if (name.length > 0) name else "REG"
  override def toString: String = {
    "REG(" + nameOpt + ")"
  }

  override def assign(src: Node) {
    if(assigned || inputs(0) != null) {
      ChiselError.error("reassignment to Reg");
    } else {
      assigned = true; super.assign(src)
    }
  }
}
