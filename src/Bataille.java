import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Bataille {
	public static void main(String args[]) {
		initGrille(grilleJoueur);
		initGrille(grilleOrdi);
		
		initGrilleComparaison();
		
		tirDejaFait.clear();
		
		System.out.println("Placez vos bateaux!");
		placerBateauxJoueur();
		System.out.println("Les bateaux sont placés! La partie commence!");
		
		PositionTableau positionATirer;
		
		do {
			afficherGrille(grilleJoueur);
			
			positionATirer = obtenirPositionTableau();
		
			tirerTorpille(grilleOrdi, positionATirer.getLigne(), positionATirer.getColonne());
			
			if(!aPerdu(grilleOrdi)) {
				System.out.print("Tir de l'ordinateur: ");
				tirAleatoireSurGrille(grilleJoueur);
			}
		} while(!aPerdu(grilleJoueur) && !aPerdu(grilleOrdi));
		
		if(aPerdu(grilleJoueur)) {
			System.out.println("Défaite! Meilleure chance la prochaine fois!");
		} else if(aPerdu(grilleOrdi)) {
			System.out.println("Victoire!");
		}
	}
	
	public static int[][] grilleOrdi = new int[10][10];
	public static int[][] grilleJoueur = new int[10][10];
	
	public static enum Direction {
		Horizontal,
		Vertical
	}
	
	private static class PositionTableau {
		private int ligne;
		private int colonne;
		
		public PositionTableau(int ligneChoix, int colonneChoix) {
			this.ligne = ligneChoix;
			this.colonne = colonneChoix;
		}
		
		public int getLigne() {
			return this.ligne;
		}
		
		public int getColonne() {
			return this.colonne;
		}
		
		public boolean pareil(PositionTableau position) {
			if(this.ligne == position.getLigne() && this.colonne == position.getColonne()) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private static class ChoixPlacementBateau extends PositionTableau {
		private Direction direction;
		
		public ChoixPlacementBateau(Direction directionChoix, int ligneChoix, int colonneChoix) {
			super(ligneChoix, colonneChoix);
			
			this.direction = directionChoix;
		}
		
		public Direction getDirection() {
			return this.direction;
		}
	}
	
	@SuppressWarnings("serial")
	public static HashMap<String, Integer> idBateaux = new HashMap<String, Integer>() {{
		put("Porte-Avions", 1);
		put("Croiseur", 2);
		put("Contre-Torpilleur", 3);	
		put("Sous-Marin", 4);
		put("Torpilleur", 5);
	}};
	
	@SuppressWarnings("serial")
	public static HashMap<Integer, Integer> grandeurBateaux = new HashMap<Integer, Integer>() {{
		put(1, 5);
		put(2, 4);
		put(3, 3);
		put(4, 3);
		put(5, 2);
	}};
	
	public static ArrayList<PositionTableau> tirDejaFait = new ArrayList<PositionTableau>();
	
	public static void placerBateauxJoueur() {
		ChoixPlacementBateau choix = null;
		boolean posOk = false;
		
		for (Map.Entry<String, Integer> bateau : idBateaux.entrySet()) {
			System.out.println("Placer votre " + bateau.getKey() + ", celui-ci est gros de " + grandeurBateaux.get(bateau.getValue()));
			
			do {
				choix = obtenirChoixPlacement();
				
				posOk = PosOk(grilleJoueur, choix.getLigne(), choix.getColonne(), choix.getDirection(), bateau.getValue());
				if(posOk) {
					placerBateau(grilleJoueur, choix.getLigne(), choix.getColonne(), choix.getDirection(), bateau.getValue());
					System.out.println(bateau.getKey() + " placé!");
				} else {
					System.out.println("Le bateau ne peut pas être placé à cet endroit!");
				}
				
				afficherGrille(grilleJoueur);
			} while(!posOk);
		}
	}
	
	public static boolean PosOk(int[][] grille, int ligne, int colonne, Direction direction, int tailleBateau) {
		if(direction == Direction.Horizontal) {
			if(colonne + tailleBateau < 9) {
				for(int i = colonne; i < colonne + tailleBateau; i++) {
					if(grille[ligne][i] != 0) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else if(direction == Direction.Vertical) {
			if(ligne + tailleBateau < 9) {
				for(int i = ligne; i < ligne + tailleBateau; i++) {
					if(grille[i][colonne] != 0) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public static void placerBateau(int[][] grille, int ligne, int colonne, Direction direction, int idBateau) {
		if(direction == Direction.Horizontal) {
			for(int i = colonne; i < colonne + grandeurBateaux.get(idBateau); i++) {
				grille[ligne][i] = idBateau;
			}
		} else if(direction == Direction.Vertical) {
			for(int i = ligne; i < ligne + grandeurBateaux.get(idBateau); i++) {
				grille[i][colonne] = idBateau;
			}
		}
	}
	
	public static void tirerTorpille(int[][] grille, int ligne, int colonne) {
		boolean touche = false;
		boolean coule = false;
		
		if(grille[ligne][colonne] != 0 && grille[ligne][colonne] != 6) {
			touche = true;
			
			if(touche) {
				int nombreBateauTouche = 0;
				
				for(int i = 0; i < 10; i++) {
					for(int j = 0; j < 10; j++) {
						if(grille[i][j] == grille[ligne][colonne]) {
							nombreBateauTouche++;
						}
					}
				}
				
				if(nombreBateauTouche - 1 <= 0) {
					coule = true;
				}
				
				grille[ligne][colonne] = 6;
			}
		} else {
			if(grille[ligne][colonne] == 0) {
				System.out.println("Tir dans l'eau!");
			} else if(grille[ligne][colonne] == 6) {
				System.out.println("Bateau déjà touché!");
			}
		}
		
		if(touche && !coule) {
			System.out.println("Touché!");
		} else if(touche && coule) {
			System.out.println("Coulé!");
		}
	}
	
	public static void tirAleatoireSurGrille(int[][] grille) {
		PositionTableau position;
		boolean dejaTire;
		
		do {
			position = new PositionTableau(randRange(0, 9), randRange(0, 9));
			
			dejaTire = false;
			
			for(int i = 0; i < tirDejaFait.size(); i++) {
				if(tirDejaFait.get(i).pareil(position)) {
					dejaTire = true;
				}
			}
			
			if(!dejaTire) {
				int positionColonne = position.getColonne() + 65;
				char charColonne = (char)positionColonne;
				
				tirerTorpille(grille, position.getLigne(), position.getColonne());
				
				System.out.println("Position du tir: " + charColonne + (position.getLigne() + 1));
				
				tirDejaFait.add(position);
			}
		} while(dejaTire);
	}
	
	public static boolean aPerdu(int[][] grille) {
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++)  {
				if(grille[i][j] != 0 && grille[i][j] != 6) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static Direction obtenirDirection() {
		String choix;
		Direction direction = null;
		
		System.out.println("Choisissez la direction du bateau (H = Horizontal, V = Vertical");
		
		do {
			choix = readString();
			
			if(choix.toLowerCase().equals("h")) {
				direction = Direction.Horizontal;
				System.out.println("Horizontal choisi!");
			} else if(choix.toLowerCase().equals("v")) {
				direction = Direction.Vertical;
				System.out.println("Vertical choisi!");
			} else {
				System.out.println("Choix invalide!");
			}
		} while(!choix.toLowerCase().equals("h") && !choix.toLowerCase().equals("v"));
		
		return direction;
	}
	
	public static PositionTableau obtenirPositionTableau()   {
		String choix;
		int ligne = -1, colonne = -1;
		
		char caractere;
		int asciiChar;
		
		System.out.println("Choisissez une case (exemple \"A1\"): ");
		
		do {
			choix = readString().toUpperCase();
			
			caractere = choix.charAt(0);
			asciiChar = (int) caractere;
			
			if((asciiChar >= 65 && asciiChar <= 90)) {
				colonne = asciiChar - 65;
			}
			
			if(choix.length() == 2) {
				caractere = choix.charAt(1);
				asciiChar = (int) caractere;
				
				if(asciiChar >= 49 && asciiChar <= 57) {
					ligne = asciiChar - 49;
				}
			} else if(choix.length() == 3) {
				if(choix.substring(1).equals("10")) {
					ligne = 10;
				}
			} else {
				System.out.println("Choix invalide!");
			}
		} while(ligne == -1 || colonne == -1);
		
		return new PositionTableau(ligne, colonne);
	}
	
	public static ChoixPlacementBateau obtenirChoixPlacement() {
		Direction direction = obtenirDirection();
		
		PositionTableau position = obtenirPositionTableau();
		
		return new ChoixPlacementBateau(direction, position.getLigne(), position.getColonne());
	}
	
	public static PositionTableau obtenirPositionTir() {
		PositionTableau position = obtenirPositionTableau();
		return position;
	}
	
	public static void initGrille(int[][] grille) {
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				grille[j][i] = 0;
			}
		}
	}
	
	public static void initGrilleComparaison() {
		int ligne;
		int colonne;
		Direction direction;
		
		boolean posOk = false;
		
		for (Map.Entry<String, Integer> bateau : idBateaux.entrySet()) {
			do {
				if(randRange(1, 10) > 5) {
					direction = Direction.Horizontal;
				} else {
					direction = Direction.Vertical;
				}
				
				ligne = randRange(0, 9);
				colonne = randRange(0, 9);
				
				posOk = PosOk(grilleOrdi, ligne, colonne, direction, grandeurBateaux.get(bateau.getValue()));
				
				if(posOk) {
					placerBateau(grilleOrdi, ligne, colonne, direction, bateau.getValue());
				}
			} while(!posOk);
	    }
		
		
	}
	
	public static Random rand = new Random();
	
	public static int randRange(int a, int b) {
		return rand.nextInt(b-a)+a;
	}
	
	public static void afficherGrille(int[][] grille) {
		System.out.print("    ");
		
		for(int i = 0; i < 10; i++) {
			System.out.print(Character.toString(i + 65) + "  ");
		}
		
		System.out.print("\n");
		
		for(int i = 0; i < 10; i++) {
			System.out.print(i + 1 + (i == 9 ? "  " : "   "));
			for(int j = 0; j < 10; j++) {
				System.out.print(grille[i][j] + "  ");
			}
			System.out.print("\n");
		}
	}
	
	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	public static String readString() {
		String res = "";
		try {
			res = br.readLine();
		} catch(Exception e) {
			System.out.println("Problème avec le read");
		}
		return res;
	}
	
	public static boolean isInt(String s) {
		return s.matches("\\d+");
	}
	
	public static int readInt() {
		while(true) {
			String s = readString();
			if(isInt(s)) {
				return Integer.parseInt(s);
			}
		}
	}
}
