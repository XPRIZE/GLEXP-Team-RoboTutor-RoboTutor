//Class to store equation info in

class Equation {
	//fields to store equation info
	private int op1;
	private int op2;
	private String op;
	private int ans;
	//ex: op1 = 8, op2 = 1, op = “add”, ans = 9
	private boolean is_counting;
	private int min;
	private int max;
	private int gap;
	private int[] seq;
 
	//constructor new equation for addition/subtraction 
	public Equation(int op1, int op2, String op){
		this.op1 = op1;
		this.op2 = op2;
		this.op = op;
 
		//can be extended for other operators
		if(op.equals("add")){
			this.ans = this.op1 + this.op2;
     	} else {
			this.ans = this.op1 - this.op2;
		}

		//not a counting problem
		this.is_counting = false;
		//default values
		this.min = 0;
		this.max = 0;
		this.gap = 0;
		this.seq = null;
	}

	//constructor for counting problem
	public Equation(int min, int max, int gap){
		//is a counting problem
		this.is_counting = true;

		this.min = min;
		this.max = max;
		this.gap = gap;
		this.seq = new int[(max - min) / gap];

		int track = min;
		for(int i = 0; i < ((max - min) / gap); i++){
			this.seq[i] = track;
			track += gap;
		}

		//default values
		this.op1 = 0;
		this.op2 = 0;
		this.op = "";
		this.ans = 0;
	}
 
	//get info
	public int get_op1(){
		return this.op1;
	} 
	public int get_op2(){
		return this.op2;
	}
	public String get_op(){
		return this.op;
	}
	public int get_ans(){
		return this.ans;
	}
	public boolean get_is_counting(){
		return this.is_counting;
	}
 
	//set parameters
	public void set_op1(int x){
		this.op1 = x;
	} 
	public void set_op2(int x){
		this.op2 = x;
	}
	public void set_op(String x){
		this.op = x;
	}
	public void set_ans(){
		if(op.equals("+")){
			this.ans = this.op1 + this.op2;
     	} else {
			this.ans = this.op1 - this.op2;
		}
	} 
	public void set_is_counting(boolean b){
		this.is_counting = b;
	}

	//printing function for testing purposes
	public void printEq(){
		if(this.op.compare("add")){
			System.out.println(this.op1 + "+" + this.op2 + "=" + this.ans);
		} else if (this.op.compare("subtract")){
			System.out.println(this.op1 + "-" + this.op2 + "=" + this.ans);
		} else {
			System.out.print("[" + this.seq[0]);
			for(int i = 1; i < this.seq.size(); i++){
				System.out.print(" " + this.seq[i]);
			}
			System.out.println("]");
		}
	}
}
