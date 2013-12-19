module DelaySuite_RegInitCondUpdate_1(input clk, input reset,
    input io_in,
    output [31:0] io_out
);

  wire [31:0] T0;
  reg res;
  wire T1;
  wire T2;

  assign io_out = T0;
  assign T0 = {31'h0/* 0*/, res};
  assign T1 = io_in ? T2 : res;
  assign T2 = res + 1'h1/* 1*/;

  always @(posedge clk) begin
    if(reset) begin
      res <= 1'h0/* 0*/;
    end else if(io_in) begin
      res <= T1;
    end
  end
endmodule
