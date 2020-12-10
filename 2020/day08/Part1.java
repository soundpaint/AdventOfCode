import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part1
{
  private enum ExitCode {
    SEG_FAULT, LOOP_DETECTED
  }

  // registers

  /** program counter */
  int pc;

  /** accumulator */
  int acc;

  abstract private class Instruction
  {
    protected String mnemonic;
    protected int arg;

    private Instruction() {
      throw new UnsupportedOperationException("empty constructor");
    }

    protected Instruction(final String mnemonic, final int arg)
    {
      this.mnemonic = mnemonic;
      this.arg = arg;
    }

    public int getArg() { return arg; }

    abstract void execute();

    public String toString()
    {
      return mnemonic + " " + arg;
    }
  }

  private class Acc extends Instruction {
    Acc(final int arg)
    {
      super("acc", arg);
    }
    public void execute()
    {
      pc++;
      acc += arg;
    }
  }

  private class Jmp extends Instruction {
    Jmp(final int arg)
    {
      super("jmp", arg);
    }
    public void execute()
    {
      pc += arg;
    }
  }

  private class Nop extends Instruction {
    Nop(final int arg)
    {
      super("nop", arg);
    }
    public void execute()
    {
      pc++;
    }
  }

  private Part1() {}

  public Instruction parse(final String unparsed)
  {
    final String tokens[] = unparsed.split(" ");
    final int arg = Integer.parseInt(tokens[1]);
    final Instruction instruction;
    switch (tokens[0]) {
    case "acc" :
      instruction = new Acc(arg);
      break;
    case "jmp":
      instruction = new Jmp(arg);
      break;
    case "nop":
      instruction = new Nop(arg);
      break;
    default:
      throw new RuntimeException();
    }
    return instruction;
  }

  private void printProgram(final Instruction[] prg)
  {
    for (int adr = 0; adr < prg.length; adr++) {
      System.out.println(String.format("0x%04X", adr) + "- " + prg[adr]);
    }
  }

  private void printRegisters()
  {
    System.out.println("pc=" + pc);
    System.out.println("acc=" + acc);
  }

  private ExitCode executeProgram(final Instruction[] prg)
  {
    pc = 0;
    acc = 0;
    final boolean seen[] = new boolean[prg.length];
    while ((pc != prg.length) && !seen[pc]) {
      seen[pc] = true;
      prg[pc].execute();
    }
    final ExitCode exitCode;
    if (pc == prg.length) {
      System.out.println("program terminated normally");
      printRegisters();
      exitCode = ExitCode.SEG_FAULT;
    } else {
      System.out.println("program terminated by loop detection");
      printRegisters();
      exitCode = ExitCode.LOOP_DETECTED;
    }
    return exitCode;
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var lines = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line);
    }
    Instruction[] prg = new Instruction[lines.size()];
    int adr = 0;
    for (final var asmInstruction : lines) {
      prg[adr++] = parse(asmInstruction);
    }
    executeProgram(prg);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
