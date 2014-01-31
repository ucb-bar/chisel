module StdlibSuite_ArbiterTest_1(
    output io_in_0_ready,
    input  io_in_0_valid,
    input [7:0] io_in_0_bits,
    output io_in_1_ready,
    input  io_in_1_valid,
    input [7:0] io_in_1_bits,
    output io_in_2_ready,
    input  io_in_2_valid,
    input [7:0] io_in_2_bits,
    output io_in_3_ready,
    input  io_in_3_valid,
    input [7:0] io_in_3_bits,
    input  io_out_ready,
    output io_out_valid,
    output[7:0] io_out_bits,
    output[1:0] io_chosen
);

  wire[1:0] T0;
  wire[1:0] T1;
  wire[1:0] T2;
  wire[1:0] T3;
  wire[7:0] T4;
  wire[7:0] T5;
  wire T6;
  wire[1:0] T7;
  wire[7:0] T8;
  wire T9;
  wire T10;
  wire T11;
  wire T12;
  wire T13;
  wire T14;
  wire T15;
  wire T16;
  wire T17;
  wire T18;
  wire T19;
  wire T20;
  wire T21;
  wire T22;
  wire T23;
  wire T24;
  wire T25;
  wire T26;
  wire T27;
  wire T28;
  wire T29;
  wire T30;

  assign io_chosen = T0;
  assign T0 = T1;
  assign T1 = io_in_0_valid ? 2'h0/* 0*/ : T2;
  assign T2 = io_in_1_valid ? 2'h1/* 1*/ : T3;
  assign T3 = io_in_2_valid ? 2'h2/* 2*/ : 2'h3/* 3*/;
  assign io_out_bits = T4;
  assign T4 = T10 ? T8 : T5;
  assign T5 = T6 ? io_in_1_bits : io_in_0_bits;
  assign T6 = T7[1'h0/* 0*/:1'h0/* 0*/];
  assign T7 = T0;
  assign T8 = T9 ? io_in_3_bits : io_in_2_bits;
  assign T9 = T7[1'h0/* 0*/:1'h0/* 0*/];
  assign T10 = T7[1'h1/* 1*/:1'h1/* 1*/];
  assign io_out_valid = T11;
  assign T11 = T16 ? T14 : T12;
  assign T12 = T13 ? io_in_1_valid : io_in_0_valid;
  assign T13 = T7[1'h0/* 0*/:1'h0/* 0*/];
  assign T14 = T15 ? io_in_3_valid : io_in_2_valid;
  assign T15 = T7[1'h0/* 0*/:1'h0/* 0*/];
  assign T16 = T7[1'h1/* 1*/:1'h1/* 1*/];
  assign io_in_3_ready = T17;
  assign T17 = T18 && io_out_ready;
  assign T18 = T19;
  assign T19 = ! T20;
  assign T20 = T21 || io_in_2_valid;
  assign T21 = io_in_0_valid || io_in_1_valid;
  assign io_in_2_ready = T22;
  assign T22 = T23 && io_out_ready;
  assign T23 = T24;
  assign T24 = ! T25;
  assign T25 = io_in_0_valid || io_in_1_valid;
  assign io_in_1_ready = T26;
  assign T26 = T27 && io_out_ready;
  assign T27 = T28;
  assign T28 = ! io_in_0_valid;
  assign io_in_0_ready = T29;
  assign T29 = T30 && io_out_ready;
  assign T30 = 1'h1/* 1*/;
endmodule

