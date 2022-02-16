import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Classe englobant la totalité de bataille navale
 */
public class Bataille {
	
	/**
	 * Fonction qui est executé au lancement de l'application
	 * @param args Contient les arguments passés au main
	 */
	public static void main(String args[]) {
		// Initialise la grille avec des 0 partout
		initGrille(grilleJoueur);
		initGrille(grilleOrdi);
		
		// Initialise la grille de l'ordinateur avec des bateaux placés aléatoirements
		initGrilleComparaison();
		
		// Vide la arraylist contenant les tirs aléatoires déjà fait
		tirDejaFait.clear();
		
		// Demande au jour de placer les bateaux
		System.out.println("Placez vos bateaux!");
		afficherGrille(grilleJoueur);
		placerBateauxJoueur();
		System.out.println("Les bateaux sont placés! La partie commence!");
		
		PositionTableau positionATirer;
		
		do {
			// On montre la grille du joueur
			afficherGrille(grilleJoueur);
			
			// On obtient la position où le joueur veut tirer
			positionATirer = obtenirPositionTableau();
		
			// On tire la torpille sur la grille de l'ordinateur avec la position obtenu
			tirerTorpille(grilleOrdi, positionATirer.getLigne(), positionATirer.getColonne());
			
			// Si l'ordinateur n'a pas perdu, il tire une torpille aléatoire sur la grille du joueur
			if(!aPerdu(grilleOrdi)) {
				System.out.print("Tir de l'ordinateur: ");
				tirAleatoireSurGrille(grilleJoueur);
			}
		} while(!aPerdu(grilleJoueur) && !aPerdu(grilleOrdi)); // On boucle tant que l'ordinateur et le joueur n'ont pas perdu
		
		if(aPerdu(grilleJoueur)) {
			// Si le jour a perdu, on affiche un message de défaite
			System.out.println("Défaite! Meilleure chance la prochaine fois!");
		} else if(aPerdu(grilleOrdi)) {
			// Si le jour a gagné on montre un message de victoire
			System.out.println("Victoire!");
		}
	}
	
	/**
	 * Déclaration de la grille de l'ordinateur
	 */
	public static int[][] grilleOrdi = new int[10][10];
	
	/**
	 * Déclaration de la grille du joueur
	 */
	public static int[][] grilleJoueur = new int[10][10];
	
	/**
	 * Déclaration d'une énumération pour les directions de bateaux possible
	 */
	public static enum Direction {
		/**
		 * Direction horizontal
		 */
		Horizontal,
		/**
		 * Direction vertical
		 */
		Vertical
	}
	
	
	/**
	 * Classe contenant les informations d'une position du tableau
	 * Cette classe contient un nombre correspondant à une ligne et une colonne.
	 * Ces données sont similaire à des coordonnées 2D (x et y) pour le tableau à 2 dimensions
	 */
	private static class PositionTableau {
		// Déclaraction des variables lignes et colonnes
		private int ligne;
		private int colonne;
		
		// Constructeur de la classe
		public PositionTableau(int ligneChoix, int colonneChoix) {
			this.ligne = ligneChoix;
			this.colonne = colonneChoix;
		}
		
		// Méthode pour obtenir la ligne
		public int getLigne() {
			return this.ligne;
		}
		
		// Méthode pour obtenir la colonne
		public int getColonne() {
			return this.colonne;
		}
		
		// Méthode pour comparer deux positions, return true si l'indice de ligne et de colonne sont pareil, sinon false
		public boolean pareil(PositionTableau position) {
			if(this.ligne == position.getLigne() && this.colonne == position.getColonne()) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Classe qui correspond au choix de placement du bateau
	 * Cette classe hérite de la classe PositionTableau et rajoute la direction du bateau
	 */
	private static class ChoixPlacementBateau extends PositionTableau {
		// Déclaration de la variable direction
		private Direction direction;
		
		// Constructeur de la classe avec héritate (super)
		public ChoixPlacementBateau(Direction directionChoix, int ligneChoix, int colonneChoix) {
			super(ligneChoix, colonneChoix);
			
			this.direction = directionChoix;
		}
		
		// Méthode pour obtenir la direction
		public Direction getDirection() {
			return this.direction;
		}
	}
	
	/**
	 * HashMap permettant de lier le nom d'un bateau à son ID
	 */
	@SuppressWarnings("serial")
	public static HashMap<String, Integer> idBateaux = new HashMap<String, Integer>() {{
		put("Porte-Avions", 1);
		put("Croiseur", 2);
		put("Contre-Torpilleur", 3);	
		put("Sous-Marin", 4);
		put("Torpilleur", 5);
	}};
	
	/**
	 * HashMap permettant de lier l'ID du bateau à sa grosseur (nombre de case)
	 */
	@SuppressWarnings("serial")
	public static HashMap<Integer, Integer> grandeurBateaux = new HashMap<Integer, Integer>() {{
		put(1, 5);
		put(2, 4);
		put(3, 3);
		put(4, 3);
		put(5, 2);
	}};
	
	/**
	 * ArrayList qui permet de stocké les tirs aléatoires de l'ordinateur
	 * Cela permet de ne pas réaliser le même tir deux fois.
	 * Évite que l'ordinateur ne tire dans l'eau sans cesse ou qu'elle tire sur une case déjà touché
	 */
	public static ArrayList<PositionTableau> tirDejaFait = new ArrayList<PositionTableau>();
	
	/**
	 * Fonction qui s'occupe de demander au joueur de placer les bateaux dans sa grille
	 */
	public static void placerBateauxJoueur() {
		// Déclaration des variables
		ChoixPlacementBateau choix = null;
		boolean posOk = false;
		
		// On boucle dans les bateaux de la HashMap "idBateaux"
		for (Map.Entry<String, Integer> bateau : idBateaux.entrySet()) {
			// On dit au joueur le bateau qu'il doit placer
			System.out.println("Placer votre " + bateau.getKey() + ", celui-ci est gros de " + grandeurBateaux.get(bateau.getValue()) + " cases");
			
			do {
				// On obtenir le choix de positionnement du joueur
				choix = obtenirChoixPlacement();
				
				// On vérifie si la position du bateau est possible
				posOk = PosOk(grilleJoueur, choix.getLigne(), choix.getColonne(), choix.getDirection(), grandeurBateaux.get(bateau.getValue()));
				
				// Si le bateau peut être placé à l'endroit fourni, on le place, sinon on le dit au joueur
				if(posOk) {
					placerBateau(grilleJoueur, choix.getLigne(), choix.getColonne(), choix.getDirection(), bateau.getValue());
					System.out.println(bateau.getKey() + " placé!");
				} else {
					System.out.println("Le bateau ne peut pas être placé à cet endroit!");
				}
				
				// On affiche la grille du joueur pour qu'il voit la position des ses bateaux
				afficherGrille(grilleJoueur);
			} while(!posOk); // On boucle tant que la position fourni n'est pas correct
		}
	}
	
	/**
	 * Fonction qui permet de vérifier si un bateau peut être placé à l'endroit voulu
	 * @param grille Grille dans lequel on veut placer le bateau
	 * @param ligne Ligne où un veut placer le bateau
	 * @param colonne Colonne où on veut placer le bateau
	 * @param direction Direction dans lequel on veut placer le bateau
	 * @param tailleBateau Taille du bateau qu'on veut placer
	 * @return true si le bateau peut être placé, sinon false
	 */
	public static boolean PosOk(int[][] grille, int ligne, int colonne, Direction direction, int tailleBateau) {
		if(direction == Direction.Horizontal) { // Si la direction est horizontal
			// Si la colonne additionné de la taille du bateau est inférieur ou égal à 10
			if(colonne + tailleBateau <= 10) {
				// On boucle à l'endroit où le bateau devrait être placé
				for(int i = colonne; i < colonne + tailleBateau; i++) {
					// Si il n'y a pas de l'eau à l'endroit où on veut le placer, on retourne false
					if(grille[ligne][i] != 0) {
						return false;
					}
				}
				// Si on se rend ici, il y a uniquement de l'eau où on veut placer le bateau, on retourne donc true
				return true;
			} else { // On retourne false si la ligne additionné de la taille du bateau est supérieur ou égal à 9
				return false;
			}
		} else if(direction == Direction.Vertical) { // Si la direction est verticale
			// Si la ligne additionné de la taille du bateau est inférieur ou égal à 10
			if(ligne + tailleBateau <= 10) {
				// On boucle à l'endroit où le bateau devrait être placé
				for(int i = ligne; i < ligne + tailleBateau; i++) {
					// Si il n'y a pas de l'eau à l'endroit où on veut le placer, on retourne false
					if(grille[i][colonne] != 0) {
						return false;
					}
				}
				// Si on se rend ici, il y a uniquement de l'eau où on veut placer le bateau, on retourne donc true
				return true;
			} else { // On retourne false si la ligne additionné de la taille du bateau est supérieur ou égal à 9
				return false;
			}
		}
		
		// Si la direction est ni horizontal, ni vertical, on retourne false
		return false;
	}
	
	/**
	 * Fonction permettant de placer un bateau dans une grille
	 * @param grille La grille où on veut placer le bateau
	 * @param ligne La ligne où on veut placer le bateau
	 * @param colonne La colonne où on veut placer le bateau
	 * @param direction La direction où on veut placer le bateau
	 * @param idBateau L'ID du bateau qu'on veut placer
	 */
	public static void placerBateau(int[][] grille, int ligne, int colonne, Direction direction, int idBateau) {
		if(direction == Direction.Horizontal) { // Si la direction est horizontal
			// On boucle à partir de la colonne jusqu'à la colonne additionné de la grandeur du bateau
			for(int i = colonne; i < colonne + grandeurBateaux.get(idBateau); i++) {
				// On met l'ID du bateau sur les bonnes cases
				grille[ligne][i] = idBateau;
			}
		} else if(direction == Direction.Vertical) { // Si la direction est verticale
			// On boucle à partir de la ligne jusqu'à la ligne additionné de la grandeur du bateau
			for(int i = ligne; i < ligne + grandeurBateaux.get(idBateau); i++) {
				// On met l'ID du bateau sur les bonnes cases
				grille[i][colonne] = idBateau;
			}
		}
	}
	
	/**
	 * Fonction qui permet de tirer une torpille sur une grille
	 * @param grille Grille où nous voulons tirer la torpille
	 * @param ligne Ligne où nous voulons tirer la torpille
	 * @param colonne Colonne où nous voulons tirer la torpille
	 */
	public static void tirerTorpille(int[][] grille, int ligne, int colonne) {
		// Déclaration des variables
		boolean touche = false;
		boolean coule = false;
		
		// Si la position n'est pas de l'eau (0) ou un bateau déjà touché (6)
		if(grille[ligne][colonne] != 0 && grille[ligne][colonne] != 6) {
			// La torpille touche un bateau
			touche = true;
			
			// Si la torpille a touché un bateau
			if(touche) {
				// On déclare et initialise une variable "nombreBateauTouche" qui nous permettra de savoir
				// combien de case il reste au bateau qu'on vient de toucher
				int nombreBateauTouche = 0;
				
				// On boucle à travers les lignes
				for(int i = 0; i < 10; i++) {
					// On boucle à travers les colonnes
					for(int j = 0; j < 10; j++) {
						// Si la case est similaire au bateau que nous venons de toucher
						if(grille[i][j] == grille[ligne][colonne]) {
							// On incrémente le nombre de case du bateau que nous venons de toucher de 1
							nombreBateauTouche++;
						}
					}
				}
				
				// Si le nombre de case touché - 1 est 0, cela veut dire que nous venons de toucher la dernière case du bateau
				if(nombreBateauTouche - 1 <= 0) {
					// Le bateau est coulé
					coule = true;
				}
				
				// On met la case de la torpille comme étant touché (6)
				grille[ligne][colonne] = 6;
			}
		} else {
			if(grille[ligne][colonne] == 0) { // Si la torpille est dans l'eau, on le montre à l'utilisateur
				System.out.println("Tir dans l'eau!");
			} else if(grille[ligne][colonne] == 6) { // Si la torpille est sur un bateau déjà touché, on le montre à l'utilisateur
				System.out.println("Bateau déjà touché!");
			}
		}
		
		if(touche && !coule) { // Si le bateau est touché, on le montre à l'utilisateur
			System.out.println("Touché!");
		} else if(touche && coule) { // Si le bateau est coulé, on le montre à l'utilisateur
			System.out.println("Coulé!");
		}
	}
	
	/**
	 * Fonction qui permet de tirer de façon aléatoire sur une grille
	 * @param grille Grille où nous voulons tirer aléatoirement
	 */
	public static void tirAleatoireSurGrille(int[][] grille) {
		// Déclaration des variable
		PositionTableau position;
		boolean dejaTire;
		
		do {
			// On obtient une position de tableau aléatoire
			position = new PositionTableau(randRange(0, 9), randRange(0, 9));
			
			// On initialise notre variable dejaTirer à false
			dejaTire = false;
			
			// On boucle à travers les tirs déjà fait
			for(int i = 0; i < tirDejaFait.size(); i++) {
				// Si la position du nouveau tir est similaire à un tir qui a déjà été effectué
				if(tirDejaFait.get(i).pareil(position)) {
					// On met la valeur de dejaTire à true
					dejaTire = true;
				}
			}
			
			// Si le tir n'a pas déjà été fait
			if(!dejaTire) {
				// On converti la position en valeur comprenable à l'utilisateur (ex: A1)
				int positionColonne = position.getColonne() + 65;
				char charColonne = (char)positionColonne;
				
				// On tire la torpille à la position obtenue aléatoirement
				tirerTorpille(grille, position.getLigne(), position.getColonne());
				
				// On montre la position du tir à l'utilisateur
				System.out.println("Position du tir: " + charColonne + (position.getLigne() + 1));
				
				// On ajoute le tir à la liste des tirs déjà fait
				tirDejaFait.add(position);
			}
		} while(dejaTire); // On boucle tant que nous n'avons pas un tir qui n'a pas déjà été fait
	}
	
	/**
	 * Fonction qui vérifie si tous les bateaux de la grille ont été coulés
	 * @param grille Grille qu'on veut vérifier si tous les bateaux ont été coulés
	 * @return Retourne si tous les bateaux de la grille ont été coulés
	 */
	public static boolean aPerdu(int[][] grille) {
		// On boucle à travers les lignes
		for(int i = 0; i < 10; i++) {
			// On boucle à travers les colonnes
			for(int j = 0; j < 10; j++)  {
				// Si la case n'est pas de l'eau (0) et la case n'est pas un bateau touché (6), les bateaux ne sont pas toutes coulés
				if(grille[i][j] != 0 && grille[i][j] != 6) {
					return false;
				}
			}
		}
		
		// Toutes les cases sont de l'eau (0) et des bateaux touchées (6), la grille a perdu, on retourne false
		return true;
	}
	
	/**
	 * Fonction qui permet d'obtenir la direction dont le joueur veut placer son bateau
	 * @return La direction dont le joueur veut placer son bateau
	 */
	public static Direction obtenirDirection() {
		// Déclaration des variables
		String choix;
		Direction direction = null;
		
		// Message à l'utilisateur lui demandant d'entrer H pour horizontal ou V pour vertical
		System.out.println("Choisissez la direction du bateau (H = Horizontal, V = Vertical");
		
		do {
			// On lit le choix de l'utilisateur
			choix = readString();
			
			if(choix.toLowerCase().equals("h")) { // Si le choix est H
				// On met la direction comme étant horizontal et on le dit à l'utilisateur
				direction = Direction.Horizontal;
				System.out.println("Horizontal choisi!");
			} else if(choix.toLowerCase().equals("v")) { // Si le choix est V
				// On met la direction comme étant vertical et on le dit à l'utilisateur
				direction = Direction.Vertical;
				System.out.println("Vertical choisi!");
			} else { // Si le choix n'est ni H ni V
				// Le choix est invalide et on le montre à l'utilisateur
				System.out.println("Choix invalide!");
			}
		} while(!choix.toLowerCase().equals("h") && !choix.toLowerCase().equals("v")); // On boucle tant que le choix n'est pas H ou V
		
		// On retourne la direction obtenu
		return direction;
	}
	
	/**
	 * Fonction qui permet d'obtenir la position du tableau choisi par le joueur
	 * @return La position du tableau choisi par le joueur
	 */
	public static PositionTableau obtenirPositionTableau()   {
		// Déclaration des variables pour la position du tableau
		String choix;
		int ligne = -1, colonne = -1;
		
		// Déclaration des variables pour la conversion de caractères on code ASCII
		char caractere;
		int asciiChar;
		
		// Message demandant au joueur de saisir une case
		System.out.println("Choisissez une case (exemple \"A1\"): ");
		
		do {
			// On obtient le choix et on le met en majuscule
			choix = readString().toUpperCase();
			
			// On obtient le premier caractère de la chaine de caractère du choix et on le converti on code ASCII
			caractere = choix.charAt(0);
			asciiChar = (int) caractere;
			
			// Si le caractère est entre A et J
			if((asciiChar >= 65 && asciiChar <= 74)) {
				// On déduit le numéro de la colonne avec le code ASCII
				colonne = asciiChar - 65;
			}
			
			// Si la longeur du choix est de 2 caractères (ex: A1)
			if(choix.length() == 2) {
				// On obtient le deuxième caractère de la chaine de caractère choix et on le converti en code ASCII
				caractere = choix.charAt(1);
				asciiChar = (int) caractere;
				
				// Si le caractère correspond de 1 à 9
				if(asciiChar >= 49 && asciiChar <= 57) {
					// On déduit la ligne avec le code ASCII
					ligne = asciiChar - 49;
				}
			} else if(choix.length() == 3) { // Si la longeur du choix est de 3 caractères (ex: A10)
				// Si les caractères après la lettre correspondent à 10
				if(choix.substring(1).equals("10")) {
					// La ligne est 9 étant donné que notre tableau commence à 0 en Java et 1 pour l'affichage
					ligne = 9;
				}
			} else { // Si le choix n'est pas de la bonne longeur, on le montre à l'utilisateur
				System.out.println("Choix invalide!");
			}
		} while(ligne == -1 || colonne == -1); // On boucle tant que la position de la ligne et de la colonne sont invalide
		
		// On retourne la position du tableau
		return new PositionTableau(ligne, colonne);
	}
	
	/**
	 * Fonction qui permet d'obtenir la position où le joueur veut placer son bateau
	 * @return Le choix de placement de bateau du joueur
	 */
	public static ChoixPlacementBateau obtenirChoixPlacement() {
		// Obtient la direction du bateau
		Direction direction = obtenirDirection();
		
		// Obtient la position du bateau
		PositionTableau position = obtenirPositionTableau();
		
		// Retourne un objet de choix de placement
		return new ChoixPlacementBateau(direction, position.getLigne(), position.getColonne());
	}
	
	/**
	 * Fonction qui permet d'obtenir la position où le joueur veut tirer
	 * @return La position où le joueur veut tirer
	 */
	public static PositionTableau obtenirPositionTir() {
		// Déclaration de la variable position et obtention de la position où le joueur veut tirer
		PositionTableau position = obtenirPositionTableau();
		
		// On retourne la position
		return position;
	}
	
	/**
	 * Fonction qui permet de mettre toutes les valeurs de la grille à 0
	 * @param grille Grille qui dont est initialiser à 0
	 */
	public static void initGrille(int[][] grille) {
		// On boucle à travers les lignes
		for(int i = 0; i < 10; i++) {
			// On boucle à travers les colonnes
			for(int j = 0; j < 10; j++) {
				// On met la valeur de la case à 0
				grille[i][j] = 0;
			}
		}
	}
	
	/**
	 * Fonction qui permet d'initialiser la grille de l'ordinateur avec des bateaux aléatoires
	 */
	public static void initGrilleComparaison() {
		// Déclaration des variables
		int ligne;
		int colonne;
		Direction direction;
		
		// Déclaration de la variable posOk qui sert à déterminer si le bateau peut être placé à l'endroit voulu
		boolean posOk = false;
		
		// On boucle à travers la HashMap contenant l'information des bateaux
		for (Map.Entry<String, Integer> bateau : idBateaux.entrySet()) {
			do {
				// Si un nombre aléatoire entre 1 et 10 est supérieur à 5, la direction est horizontal
				// Il y a probablement une meilleure méthode pour un nombre aléatoire ayant 50% de chance, mais cela fonctionne bien
				if(randRange(1, 10) > 5) {
					direction = Direction.Horizontal;
				} else { // Sinon la direction est vertical
					direction = Direction.Vertical;
				}
				
				// On obtient une position de ligne entre 0 et 9
				ligne = randRange(0, 9);
				
				// On obtient une position de colonne entre 0 et 9
				colonne = randRange(0, 9);
				
				// On regarde si la position aléatoire est valide
				posOk = PosOk(grilleOrdi, ligne, colonne, direction, grandeurBateaux.get(bateau.getValue()));
				
				// Si la position est valide, on place le bateau
				if(posOk) {
					placerBateau(grilleOrdi, ligne, colonne, direction, bateau.getValue());
				}
			} while(!posOk); // On boucle tant que la position du bateau n'est pas correct
	    }
	}
	
	/**
	 * Déclaration d'un Random utilisé pour avoir des nombres aléatoires
	 */
	public static Random rand = new Random();
	
	/**
	 * Fonction qui permet d'obtenir un nombre entier entre a et b
	 * @param a Le plus bas nombre possible
	 * @param b Le plus haut nombre possible
	 * @return Un nombre entier entre a et b
	 */
	public static int randRange(int a, int b) {
		return rand.nextInt(b-a)+a;
	}
	
	/**
	 * Fonction qui permet d'afficher une grille
	 * @param grille Grille qu'On désire afficher
	 */
	public static void afficherGrille(int[][] grille) {
		// Print des espaces pour l'alignement
		System.out.print("    ");
		
		// Boucle qui permet d'afficher les lettres de A à I
		for(int i = 0; i < 10; i++) {
			System.out.print(Character.toString(i + 65) + "  ");
		}
		
		// Retour à la ligne (newline)
		System.out.print("\n");
		
		// Boucle à travers les lignes
		for(int i = 0; i < 10; i++) {
			// Print le numero de ligne
			// La ligne #10 à une espace de moins pour l'alignement
			System.out.print(i + 1 + (i == 9 ? "  " : "   "));
			
			// Boucle à travers les colonnes
			for(int j = 0; j < 10; j++) {
				// Print le contenu de la grille
				System.out.print(grille[i][j] + "  ");
			}
			
			// Retour à la ligne (newline)
			System.out.print("\n");
		}
	}
	
	/**
	 * BufferedReader qui est utilisé pour lire les touches du clavier
	 */
	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	/**
	 * Fonction qui permet d'obtenir une chaine de caractère tapée au clavier
	 * @return La String tapée au clavier
	 */
	public static String readString() {
		// Déclaration de la variable de la String
		String res = "";
		
		try { // On tente de lire le contenu du BufferedReader
			res = br.readLine();
		} catch(Exception e) { // Si il y a un problème (exception) on le dit au joueur
			System.out.println("Problème avec le read");
		}
		
		// Retourne la String tapée au clavier
		return res;
	}
	
	/**
	 * Fonction qui permet de vérifier sur une String est un nombre
	 * @param s String qu'on veut vérifier si il s'agit d'un nombre
	 * @return Si la String est un nombre, return true, sinon return false
	 */
	public static boolean isInt(String s) {
		// Retourne si la String est un nombre
		return s.matches("\\d+");
	}
	
	/**
	 * Fonction qui permet de lire un nombre à l'aide du clavier
	 * @return Le nombre lu
	 */
	public static int readInt() {
		// Demande une chaine de caractère tant qu'il ne s'agit pas d'un nombre
		while(true) {
			// Lit la String
			String s = readString();
			
			// Si il s'agit d'un nombre, on retourne le nombre, sinon on redemande une String
			if(isInt(s)) {
				return Integer.parseInt(s);
			}
		}
	}
}
