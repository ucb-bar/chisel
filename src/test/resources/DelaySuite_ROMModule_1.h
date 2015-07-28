#ifndef __DelaySuite_ROMModule_1__
#define __DelaySuite_ROMModule_1__

#include "emulator.h"

class DelaySuite_ROMModule_1_t : public mod_t {
 private:
  val_t __rand_seed;
  void __srand(val_t seed) { __rand_seed = seed; }
  val_t __rand_val() { return ::__rand_val(&__rand_seed); }
 public:
  dat_t<2> DelaySuite_ROMModule_1__io_addr;
  dat_t<4> DelaySuite_ROMModule_1__io_out;
  mem_t<4,3> T1;
  int clk;
  int clk_cnt;
  int last_dump_time;

  void init ( val_t rand_init = 0 );
  void clock_lo ( dat_t<1> reset );
  void clock_hi ( dat_t<1> reset );
  int clock ( dat_t<1> reset );
  mod_t* clone();
  bool set_circuit_from(mod_t* src);
  void print ( FILE* f );
  void print ( std::ostream& s );
  void dump ( FILE* f, int t );
  void dump_init ( FILE* f );

};

class DelaySuite_ROMModule_1_api_t : public mod_api_t {
  void init_mapping_table();
};



#endif
