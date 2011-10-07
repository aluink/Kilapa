package net.aluink.chess.suicide.game.lmg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import net.aluink.chess.board.Piece;
import net.aluink.chess.board.Piece.Color;
import net.aluink.chess.suicide.game.Board;
import net.aluink.chess.suicide.game.Move;

public class SuicideLMG implements LegalMoveGenerator {

	public static final int UP = 8;
	public static final int DOWN = -8;
	public static final int LEFT = -1;
	public static final int RIGHT = 1;
	
	long allBoard;
	long thisBoard;
	long otherBoard;
	long [][] bbs;
	
	Board b;
	
	static long [] kingMasks = {
			770L,
			1797L,
			3594L,
			7188L,
			14376L,
			28752L,
			57504L,
			49216L,
			197123L,
			460039L,
			920078L,
			1840156L,
			3680312L,
			7360624L,
			14721248L,
			12599488L,
			50463488L,
			117769984L,
			235539968L,
			471079936L,
			942159872L,
			1884319744L,
			3768639488L,
			3225468928L,
			12918652928L,
			30149115904L,
			60298231808L,
			120596463616L,
			241192927232L,
			482385854464L,
			964771708928L,
			825720045568L,
			3307175149568L,
			7718173671424L,
			15436347342848L,
			30872694685696L,
			61745389371392L,
			123490778742784L,
			246981557485568L,
			211384331665408L,
			846636838289408L,
			1975852459884544L,
			3951704919769088L,
			7903409839538176L,
			15806819679076352L,
			31613639358152704L,
			63227278716305408L,
			54114388906344448L,
			216739030602088448L,
			505818229730443264L,
			1011636459460886528L,
			2023272918921773056L,
			4046545837843546112L,
			8093091675687092224L,
			-2260560722335367168L,
			-4593460513685372928L,
			144959613005987840L,
			362258295026614272L,
			724516590053228544L,
			1449033180106457088L,
			2898066360212914176L,
			5796132720425828352L,
			-6854478632857894912L,
			4665729213955833856L
	};

	long [] knightMasks = {
			132096L,
			329728L,
			659712L,
			1319424L,
			2638848L,
			5277696L,
			10489856L,
			4202496L,
			33816580L,
			84410376L,
			168886289L,
			337772578L,
			675545156L,
			1351090312L,
			2685403152L,
			1075839008L,
			8657044482L,
			21609056261L,
			43234889994L,
			86469779988L,
			172939559976L,
			345879119952L,
			687463207072L,
			275414786112L,
			2216203387392L,
			5531918402816L,
			11068131838464L,
			22136263676928L,
			44272527353856L,
			88545054707712L,
			175990581010432L,
			70506185244672L,
			567348067172352L,
			1416171111120896L,
			2833441750646784L,
			5666883501293568L,
			11333767002587136L,
			22667534005174272L,
			45053588738670592L,
			18049583422636032L,
			145241105196122112L,
			362539804446949376L,
			725361088165576704L,
			1450722176331153408L,
			2901444352662306816L,
			5802888705324613632L,
			-6913025356609880064L,
			4620693356194824192L,
			288234782788157440L,
			576469569871282176L,
			1224997833292120064L,
			2449995666584240128L,
			4899991333168480256L,
			-8646761407372591104L,
			1152939783987658752L,
			2305878468463689728L,
			1128098930098176L,
			2257297371824128L,
			4796069720358912L,
			9592139440717824L,
			19184278881435648L,
			38368557762871296L,
			4679521487814656L,
			9077567998918656L
	};
	
	/* (non-Javadoc)
	 * @see net.aluink.chess.suicide.game.BoundChecker#getLegalMoves(net.aluink.chess.suicide.game.Board)
	 */
	@Override
	public Stack<Move> getLegalMoves(Board b) {
		this.b = b;
		Stack<Move> moves = new Stack<Move>();
		AttackingStatus attacking = new AttackingStatus();
		
		// Setup all the boards
		bbs = b.getBitBoards();
		int index = b.getTurn().getIndex();
		thisBoard = bbs[index][0] | bbs[index][1] | bbs[index][2] | bbs[index][3] | bbs[index][4] | bbs[index][5];
		index = (index+1) % 2;
		otherBoard = bbs[index][0] | bbs[index][1] | bbs[index][2] | bbs[index][3] | bbs[index][4] | bbs[index][5];
		allBoard = thisBoard | otherBoard;
		
		int bitboard = (int) (thisBoard & 0xFFFFFFFFL);
		for(int j = 0;j <= 32;j += 32){
			while(bitboard != 0){
				int i = Integer.numberOfTrailingZeros(bitboard)+j;
				bitboard &= bitboard -1 ;
				Piece p = b.getPos(i);
				if(p == null || p.getColor() != b.getTurn())
					continue;
				
				switch(p.getType()){
					case KING:
						getKingMoves(i, attacking, moves);
						break;
					case PAWN:
						getPawnMoves(i, attacking, moves);
						break;
					case ROOK:
						getRookMoves(i, attacking, moves);
						break;
					case KNIGHT:
						getKnightMoves(i, attacking, moves);
						break;
					case BISHOP:
						getBishopMoves(i, attacking, moves);
						break;
					case QUEEN:
						getBishopMoves(i, attacking, moves);
						getRookMoves(i, attacking, moves);
						break;
				}
			}
			bitboard = (int) (thisBoard >> 32);
		}
		return moves;
	}

	private void getBishopMoves(int start, AttackingStatus attacking, List<Move> moves) {
		getRayMoves(start, attacking, moves, Arrays.asList(
				new RayInfo(new CompoundBoundChecker(Arrays.asList(new LessThan(64),new ModNotEqual(8,7))),UP+LEFT),
				new RayInfo(new CompoundBoundChecker(Arrays.asList(new GreaterThan(0),new ModNotEqual(8,7))),DOWN+LEFT),
				new RayInfo(new CompoundBoundChecker(Arrays.asList(new LessThan(64),new ModNotEqual(8,0))),UP+RIGHT),
				new RayInfo(new CompoundBoundChecker(Arrays.asList(new GreaterThan(0),new ModNotEqual(8,0))),DOWN+RIGHT))
		);
	}

	private void getKnightMoves(int start, AttackingStatus attacking, List<Move> moves) {
		long mask = knightMasks[start];
		long attack = mask & otherBoard;
		
		if(attack != 0 && !attacking.b){
			attacking.set();
			moves.clear();
		}
		
		if(!attacking.b){
			moves.addAll(getNonRayMoves(start, mask & ~allBoard));
		} else {
			moves.addAll(getNonRayMoves(start, attack));
		}	
	}

	private void getRayMoves(int start, AttackingStatus attacking, List<Move> moves, List<RayInfo> rayInfos){
		int tmp;
		if(!attacking.b){
			for(RayInfo ri : rayInfos){
				tmp = start + ri.dir;
				while (ri.bc.inBounds(tmp)){
					if(getRayNotAttacking(start, tmp, attacking, moves))
						break;
					tmp += ri.dir;
				}
			}
		} else {
			for(RayInfo ri : rayInfos){
				tmp = start + ri.dir;
				while (ri.bc.inBounds(tmp)){
					if(getRayAttacking(start, tmp, moves))
						break;
					tmp += ri.dir;
				}
			}
		}
	}
	
	private void getRookMoves(int start, AttackingStatus attacking, List<Move> moves) {
		getRayMoves(start, attacking, moves,
			Arrays.asList(
				new RayInfo(new LessThan(64),UP),
				new RayInfo(new GreaterThan(0),DOWN),
				new RayInfo(new ModNotEqual(8,7),LEFT),
				new RayInfo(new ModNotEqual(8,0),RIGHT)
			)
		);		
	}
	private boolean getRayAttacking(int start, int tmp, List<Move> moves){
		if(b.getPos(tmp) != null){
			if(b.getPos(tmp).getColor() != b.getTurn())
				moves.add(new Move(start, tmp));
			return true;
		}
		return false;
	}
	
	class AttackingStatus {
		boolean b;
		
		public AttackingStatus(){
			b = false;
		}
		
		public void set(){
			b = true;
		}
	}
	
	private boolean getRayNotAttacking(int start, int tmp, AttackingStatus attacking, List<Move> moves){
		if(b.getPos(tmp) == null){
			if(!attacking.b)
				moves.add(new Move(start, tmp));
			return false;
		} else if(b.getPos(tmp).getColor() != b.getTurn()){
			if(!attacking.b){
				attacking.set();
				moves.clear();
			}
			moves.add(new Move(start, tmp));			
		}
		return true;
	}
	
	private void getPawnAttack(int start, int tmp, AttackingStatus attacking, List<Move> moves, boolean promo){
		if(tmp == b.getEnpassantPos()){
			if(!attacking.b){
				attacking.set();
				moves.clear();
			}
			moves.add(new Move(start, tmp, true));
		}
		if(b.getPos(tmp) != null && b.getPos(tmp).getColor() != b.getTurn()){
			if(!attacking.b){
				attacking.set();
				moves.clear();
			}
			if(promo){
				moves.addAll(Arrays.asList(Move.promoSet(start, tmp, b.getTurn())));
			} else {
				moves.add(new Move(start, tmp));
			}
		} 
		
	}
	
	private void getPawnMove(int start, int tmp, List<Move> moves, boolean promo){
		if(b.getPos(tmp) == null){
			if(promo){
				moves.addAll(Arrays.asList(Move.promoSet(start, tmp, b.getTurn())));
			} else {
				if(Math.abs(start-tmp) == 16){
					moves.add(new Move(start, tmp));
				} else {
					moves.add(new Move(start, tmp));
				}
			}
		}
	}
	
	private void getPawnMoves(int start, AttackingStatus attacking, List<Move> moves) {
		int row = start/8;
		boolean starting, promo;
		int dir;		
		if(b.getTurn() == Color.WHITE){
			dir = 8;
			starting = row == 1;
			promo = row == 6;
		}
		else{
			dir = -8;
			starting = row == 6;
			promo = row == 1;
		}
		
		if(start%8 != 0)
			getPawnAttack(start, start+dir+LEFT, attacking, moves, promo);
		if(start%8 != 7)
			getPawnAttack(start, start+dir+RIGHT, attacking, moves, promo);
		
		if(!attacking.b){
			getPawnMove(start, start+dir, moves, promo);
			if(starting)
				getPawnMove(start, start+dir+dir, moves, promo);
		}
	}

	private void getKingMoves(int p, AttackingStatus attacking, List<Move> moves) {
		long mask = kingMasks[p];
		long attack = mask & otherBoard;
		
		if(attack != 0 && !attacking.b){
			attacking.set();
			moves.clear();
		}
		
		if(!attacking.b){
			moves.addAll(getNonRayMoves(p, mask & ~allBoard));
		} else {
			moves.addAll(getNonRayMoves(p, attack));
		}
	}
	
//	int m = (int) (mask & 0xFFFFFFFFL);
//	for(int i = 0;i < 33;i+=32){
//		while(m != 0){
//			int p = Integer.numberOfTrailingZeros(m)+i;
//			m &= m-1;
//	
//	
//		}
//		m = (int) mask >> 32;
//	}
	
	/*
	 * For kings and knights
	 */
	static List<Move> getNonRayMoves(int start, long mask){
		List<Move> lst = new ArrayList<Move>();
		int m = (int) (mask & 0xFFFFFFFFL);
		for(int i = 0;i < 33;i+=32){
			while(m != 0){
				int p = Integer.numberOfTrailingZeros(m)+i;
				m &= m-1;

				lst.add(new Move(start, p));
			}
			m = (int) (mask >> 32);
		}
		return lst;
	}
	
	public static void main(String[] args) {
		for(int i = 0;i < 64;i++){
			Board.printBitboard(SuicideLMG.kingMasks[i]);
		}
	}
	
//	public static void main(String[] args) {
//		System.out.println("{");
//		for(int start = 0;start < 64;start++){
//			long b = 0L;
//			
//			System.out.println(b + "L,");
//		}
//		System.out.println("};");
//	}
	
}
