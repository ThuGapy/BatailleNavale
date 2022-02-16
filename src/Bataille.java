import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Classe englobant la totalit� de bataille navale
 */
public class Bataille {
	
	/**
	 * Fonction qui est execut� au lancement de l'application
	 * @param args Contient les arguments pass�s au main
	 */
	public static void main(String args[]) {
		// Initialise la grille avec des 0 partout
		initGrille(grilleJoueur);
		initGrille(grilleOrdi);
		
		// Initialise la grille de l'ordinateur avec des bateaux plac�s al�atoirements
		initGrilleComparaison();
		
		// Vide la arraylist contenant les tirs al�atoires d�j� fait
		tirDejaFait.clear();
		
		// Demande au jour de placer les bateaux
		System.out.println("Placez vos bateaux!");
		afficherGrille(grilleJoueur);
		placerBateauxJoueur();
		System.out.println("Les bateaux sont plac�s! La partie commence!");
		
		PositionTableau positionATirer;
		
		do {
			// On montre la grille du joueur
			afficherGrille(grilleJoueur);
			
			// On obtient la position o� le joueur veut tirer
			positionATirer = obtenirPositionTableau();
		
			// On tire la torpille sur la grille de l'ordinateur avec la position obtenu
			tirerTorpille(grilleOrdi, positionATirer.getLigne(), positionATirer.getColonne());
			
			// Si l'ordinateur n'a pas perdu, il tire une torpille al�atoire sur la grille du joueur
			if(!aPerdu(grilleOrdi)) {
				System.out.print("Tir de l'ordinateur: ");
				tirAleatoireSurGrille(grilleJoueur);
			}
		} while(!aPerdu(grilleJoueur) && !aPerdu(grilleOrdi)); // On boucle tant que l'ordinateur et le joueur n'ont pas perdu
		
		if(aPerdu(grilleJoueur)) {
			// Si le jour a perdu, on affiche un message de d�faite
			System.out.println("D�faite! Meilleure chance la prochaine fois!");
		} else if(aPerdu(grilleOrdi)) {
			// Si le jour a gagn� on montre un message de victoire
			System.out.println("Victoire!");
		}
	}
	
	/**
	 * D�claration de la grille de l'ordinateur
	 */
	public static int[][] grilleOrdi = new int[10][10];
	
	/**
	 * D�claration de la grille du joueur
	 */
	public static int[][] grilleJoueur = new int[10][10];
	
	/**
	 * D�claration d'une �num�ration pour les directions de bateaux possible
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
	 * Cette classe contient un nombre correspondant � une ligne et une colonne.
	 * Ces donn�es sont similaire � des coordonn�es 2D (x et y) pour le tableau � 2 dimensions
	 */
	private static class PositionTableau {
		// D�claraction des variables lignes et colonnes
		private int ligne;
		private int colonne;
		
		// Constructeur de la classe
		public PositionTableau(int ligneChoix, int colonneChoix) {
			this.ligne = ligneChoix;
			this.colonne = colonneChoix;
		}
		
		// M�thode pour obtenir la ligne
		public int getLigne() {
			return this.ligne;
		}
		
		// M�thode pour obtenir la colonne
		public int getColonne() {
			return this.colonne;
		}
		
		// M�thode pour comparer deux positions, return true si l'indice de ligne et de colonne sont pareil, sinon false
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
	 * Cette classe h�rite de la classe PositionTableau et rajoute la direction du bateau
	 */
	private static class ChoixPlacementBateau extends PositionTableau {
		// D�claration de la variable direction
		private Direction direction;
		
		// Constructeur de la classe avec h�ritate (super)
		public ChoixPlacementBateau(Direction directionChoix, int ligneChoix, int colonneChoix) {
			super(ligneChoix, colonneChoix);
			
			this.direction = directionChoix;
		}
		
		// M�thode pour obtenir la direction
		public Direction getDirection() {
			return this.direction;
		}
	}
	
	/**
	 * HashMap permettant de lier le nom d'un bateau � son ID
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
	 * HashMap permettant de lier l'ID du bateau � sa grosseur (nombre de case)
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
	 * ArrayList qui permet de stock� les tirs al�atoires de l'ordinateur
	 * Cela permet de ne pas r�aliser le m�me tir deux fois.
	 * �vite que l'ordinateur ne tire dans l'eau sans cesse ou qu'elle tire sur une case d�j� touch�
	 */
	public static ArrayList<PositionTableau> tirDejaFait = new ArrayList<PositionTableau>();
	
	/**
	 * Fonction qui s'occupe de demander au joueur de placer les bateaux dans sa grille
	 */
	public static void placerBateauxJoueur() {
		// D�claration des variables
		ChoixPlacementBateau choix = null;
		boolean posOk = false;
		
		// On boucle dans les bateaux de la HashMap "idBateaux"
		for (Map.Entry<String, Integer> bateau : idBateaux.entrySet()) {
			// On dit au joueur le bateau qu'il doit placer
			System.out.println("Placer votre " + bateau.getKey() + ", celui-ci est gros de " + grandeurBateaux.get(bateau.getValue()) + " cases");
			
			do {
				// On obtenir le choix de positionnement du joueur
				choix = obtenirChoixPlacement();
				
				// On v�rifie si la position du bateau est possible
				posOk = PosOk(grilleJoueur, choix.getLigne(), choix.getColonne(), choix.getDirection(), grandeurBateaux.get(bateau.getValue()));
				
				// Si le bateau peut �tre plac� � l'endroit fourni, on le place, sinon on le dit au joueur
				if(posOk) {
					placerBateau(grilleJoueur, choix.getLigne(), choix.getColonne(), choix.getDirection(), bateau.getValue());
					System.out.println(bateau.getKey() + " plac�!");
				} else {
					System.out.println("Le bateau ne peut pas �tre plac� � cet endroit!");
				}
				
				// On affiche la grille du joueur pour qu'il voit la position des ses bateaux
				afficherGrille(grilleJoueur);
			} while(!posOk); // On boucle tant que la position fourni n'est pas correct
		}
	}
	
	/**
	 * Fonction qui permet de v�rifier si un bateau peut �tre plac� � l'endroit voulu
	 * @param grille Grille dans lequel on veut placer le bateau
	 * @param ligne Ligne o� un veut placer le bateau
	 * @param colonne Colonne o� on veut placer le bateau
	 * @param direction Direction dans lequel on veut placer le bateau
	 * @param tailleBateau Taille du bateau qu'on veut placer
	 * @return true si le bateau peut �tre plac�, sinon false
	 */
	public static boolean PosOk(int[][] grille, int ligne, int colonne, Direction direction, int tailleBateau) {
		if(direction == Direction.Horizontal) { // Si la direction est horizontal
			// Si la colonne additionn� de la taille du bateau est inf�rieur ou �gal � 10
			if(colonne + tailleBateau <= 10) {
				// On boucle � l'endroit o� le bateau devrait �tre plac�
				for(int i = colonne; i < colonne + tailleBateau; i++) {
					// Si il n'y a pas de l'eau � l'endroit o� on veut le placer, on retourne false
					if(grille[ligne][i] != 0) {
						return false;
					}
				}
				// Si on se rend ici, il y a uniquement de l'eau o� on veut placer le bateau, on retourne donc true
				return true;
			} else { // On retourne false si la ligne additionn� de la taille du bateau est sup�rieur ou �gal � 9
				return false;
			}
		} else if(direction == Direction.Vertical) { // Si la direction est verticale
			// Si la ligne additionn� de la taille du bateau est inf�rieur ou �gal � 10
			if(ligne + tailleBateau <= 10) {
				// On boucle � l'endroit o� le bateau devrait �tre plac�
				for(int i = ligne; i < ligne + tailleBateau; i++) {
					// Si il n'y a pas de l'eau � l'endroit o� on veut le placer, on retourne false
					if(grille[i][colonne] != 0) {
						return false;
					}
				}
				// Si on se rend ici, il y a uniquement de l'eau o� on veut placer le bateau, on retourne donc true
				return true;
			} else { // On retourne false si la ligne additionn� de la taille du bateau est sup�rieur ou �gal � 9
				return false;
			}
		}
		
		// Si la direction est ni horizontal, ni vertical, on retourne false
		return false;
	}
	
	/**
	 * Fonction permettant de placer un bateau dans une grille
	 * @param grille La grille o� on veut placer le bateau
	 * @param ligne La ligne o� on veut placer le bateau
	 * @param colonne La colonne o� on veut placer le bateau
	 * @param direction La direction o� on veut placer le bateau
	 * @param idBateau L'ID du bateau qu'on veut placer
	 */
	public static void placerBateau(int[][] grille, int ligne, int colonne, Direction direction, int idBateau) {
		if(direction == Direction.Horizontal) { // Si la direction est horizontal
			// On boucle � partir de la colonne jusqu'� la colonne additionn� de la grandeur du bateau
			for(int i = colonne; i < colonne + grandeurBateaux.get(idBateau); i++) {
				// On met l'ID du bateau sur les bonnes cases
				grille[ligne][i] = idBateau;
			}
		} else if(direction == Direction.Vertical) { // Si la direction est verticale
			// On boucle � partir de la ligne jusqu'� la ligne additionn� de la grandeur du bateau
			for(int i = ligne; i < ligne + grandeurBateaux.get(idBateau); i++) {
				// On met l'ID du bateau sur les bonnes cases
				grille[i][colonne] = idBateau;
			}
		}
	}
	
	/**
	 * Fonction qui permet de tirer une torpille sur une grille
	 * @param grille Grille o� nous voulons tirer la torpille
	 * @param ligne Ligne o� nous voulons tirer la torpille
	 * @param colonne Colonne o� nous voulons tirer la torpille
	 */
	public static void tirerTorpille(int[][] grille, int ligne, int colonne) {
		// D�claration des variables
		boolean touche = false;
		boolean coule = false;
		
		// Si la position n'est pas de l'eau (0) ou un bateau d�j� touch� (6)
		if(grille[ligne][colonne] != 0 && grille[ligne][colonne] != 6) {
			// La torpille touche un bateau
			touche = true;
			
			// Si la torpille a touch� un bateau
			if(touche) {
				// On d�clare et initialise une variable "nombreBateauTouche" qui nous permettra de savoir
				// combien de case il reste au bateau qu'on vient de toucher
				int nombreBateauTouche = 0;
				
				// On boucle � travers les lignes
				for(int i = 0; i < 10; i++) {
					// On boucle � travers les colonnes
					for(int j = 0; j < 10; j++) {
						// Si la case est similaire au bateau que nous venons de toucher
						if(grille[i][j] == grille[ligne][colonne]) {
							// On incr�mente le nombre de case du bateau que nous venons de toucher de 1
							nombreBateauTouche++;
						}
					}
				}
				
				// Si le nombre de case touch� - 1 est 0, cela veut dire que nous venons de toucher la derni�re case du bateau
				if(nombreBateauTouche - 1 <= 0) {
					// Le bateau est coul�
					coule = true;
				}
				
				// On met la case de la torpille comme �tant touch� (6)
				grille[ligne][colonne] = 6;
			}
		} else {
			if(grille[ligne][colonne] == 0) { // Si la torpille est dans l'eau, on le montre � l'utilisateur
				System.out.println("Tir dans l'eau!");
			} else if(grille[ligne][colonne] == 6) { // Si la torpille est sur un bateau d�j� touch�, on le montre � l'utilisateur
				System.out.println("Bateau d�j� touch�!");
			}
		}
		
		if(touche && !coule) { // Si le bateau est touch�, on le montre � l'utilisateur
			System.out.println("Touch�!");
		} else if(touche && coule) { // Si le bateau est coul�, on le montre � l'utilisateur
			System.out.println("Coul�!");
		}
	}
	
	/**
	 * Fonction qui permet de tirer de fa�on al�atoire sur une grille
	 * @param grille Grille o� nous voulons tirer al�atoirement
	 */
	public static void tirAleatoireSurGrille(int[][] grille) {
		// D�claration des variable
		PositionTableau position;
		boolean dejaTire;
		
		do {
			// On obtient une position de tableau al�atoire
			position = new PositionTableau(randRange(0, 9), randRange(0, 9));
			
			// On initialise notre variable dejaTirer � false
			dejaTire = false;
			
			// On boucle � travers les tirs d�j� fait
			for(int i = 0; i < tirDejaFait.size(); i++) {
				// Si la position du nouveau tir est similaire � un tir qui a d�j� �t� effectu�
				if(tirDejaFait.get(i).pareil(position)) {
					// On met la valeur de dejaTire � true
					dejaTire = true;
				}
			}
			
			// Si le tir n'a pas d�j� �t� fait
			if(!dejaTire) {
				// On converti la position en valeur comprenable � l'utilisateur (ex: A1)
				int positionColonne = position.getColonne() + 65;
				char charColonne = (char)positionColonne;
				
				// On tire la torpille � la position obtenue al�atoirement
				tirerTorpille(grille, position.getLigne(), position.getColonne());
				
				// On montre la position du tir � l'utilisateur
				System.out.println("Position du tir: " + charColonne + (position.getLigne() + 1));
				
				// On ajoute le tir � la liste des tirs d�j� fait
				tirDejaFait.add(position);
			}
		} while(dejaTire); // On boucle tant que nous n'avons pas un tir qui n'a pas d�j� �t� fait
	}
	
	/**
	 * Fonction qui v�rifie si tous les bateaux de la grille ont �t� coul�s
	 * @param grille Grille qu'on veut v�rifier si tous les bateaux ont �t� coul�s
	 * @return Retourne si tous les bateaux de la grille ont �t� coul�s
	 */
	public static boolean aPerdu(int[][] grille) {
		// On boucle � travers les lignes
		for(int i = 0; i < 10; i++) {
			// On boucle � travers les colonnes
			for(int j = 0; j < 10; j++)  {
				// Si la case n'est pas de l'eau (0) et la case n'est pas un bateau touch� (6), les bateaux ne sont pas toutes coul�s
				if(grille[i][j] != 0 && grille[i][j] != 6) {
					return false;
				}
			}
		}
		
		// Toutes les cases sont de l'eau (0) et des bateaux touch�es (6), la grille a perdu, on retourne false
		return true;
	}
	
	/**
	 * Fonction qui permet d'obtenir la direction dont le joueur veut placer son bateau
	 * @return La direction dont le joueur veut placer son bateau
	 */
	public static Direction obtenirDirection() {
		// D�claration des variables
		String choix;
		Direction direction = null;
		
		// Message � l'utilisateur lui demandant d'entrer H pour horizontal ou V pour vertical
		System.out.println("Choisissez la direction du bateau (H = Horizontal, V = Vertical");
		
		do {
			// On lit le choix de l'utilisateur
			choix = readString();
			
			if(choix.toLowerCase().equals("h")) { // Si le choix est H
				// On met la direction comme �tant horizontal et on le dit � l'utilisateur
				direction = Direction.Horizontal;
				System.out.println("Horizontal choisi!");
			} else if(choix.toLowerCase().equals("v")) { // Si le choix est V
				// On met la direction comme �tant vertical et on le dit � l'utilisateur
				direction = Direction.Vertical;
				System.out.println("Vertical choisi!");
			} else { // Si le choix n'est ni H ni V
				// Le choix est invalide et on le montre � l'utilisateur
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
		// D�claration des variables pour la position du tableau
		String choix;
		int ligne = -1, colonne = -1;
		
		// D�claration des variables pour la conversion de caract�res on code ASCII
		char caractere;
		int asciiChar;
		
		// Message demandant au joueur de saisir une case
		System.out.println("Choisissez une case (exemple \"A1\"): ");
		
		do {
			// On obtient le choix et on le met en majuscule
			choix = readString().toUpperCase();
			
			// On obtient le premier caract�re de la chaine de caract�re du choix et on le converti on code ASCII
			caractere = choix.charAt(0);
			asciiChar = (int) caractere;
			
			// Si le caract�re est entre A et J
			if((asciiChar >= 65 && asciiChar <= 74)) {
				// On d�duit le num�ro de la colonne avec le code ASCII
				colonne = asciiChar - 65;
			}
			
			// Si la longeur du choix est de 2 caract�res (ex: A1)
			if(choix.length() == 2) {
				// On obtient le deuxi�me caract�re de la chaine de caract�re choix et on le converti en code ASCII
				caractere = choix.charAt(1);
				asciiChar = (int) caractere;
				
				// Si le caract�re correspond de 1 � 9
				if(asciiChar >= 49 && asciiChar <= 57) {
					// On d�duit la ligne avec le code ASCII
					ligne = asciiChar - 49;
				}
			} else if(choix.length() == 3) { // Si la longeur du choix est de 3 caract�res (ex: A10)
				// Si les caract�res apr�s la lettre correspondent � 10
				if(choix.substring(1).equals("10")) {
					// La ligne est 9 �tant donn� que notre tableau commence � 0 en Java et 1 pour l'affichage
					ligne = 9;
				}
			} else { // Si le choix n'est pas de la bonne longeur, on le montre � l'utilisateur
				System.out.println("Choix invalide!");
			}
		} while(ligne == -1 || colonne == -1); // On boucle tant que la position de la ligne et de la colonne sont invalide
		
		// On retourne la position du tableau
		return new PositionTableau(ligne, colonne);
	}
	
	/**
	 * Fonction qui permet d'obtenir la position o� le joueur veut placer son bateau
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
	 * Fonction qui permet d'obtenir la position o� le joueur veut tirer
	 * @return La position o� le joueur veut tirer
	 */
	public static PositionTableau obtenirPositionTir() {
		// D�claration de la variable position et obtention de la position o� le joueur veut tirer
		PositionTableau position = obtenirPositionTableau();
		
		// On retourne la position
		return position;
	}
	
	/**
	 * Fonction qui permet de mettre toutes les valeurs de la grille � 0
	 * @param grille Grille qui dont est initialiser � 0
	 */
	public static void initGrille(int[][] grille) {
		// On boucle � travers les lignes
		for(int i = 0; i < 10; i++) {
			// On boucle � travers les colonnes
			for(int j = 0; j < 10; j++) {
				// On met la valeur de la case � 0
				grille[i][j] = 0;
			}
		}
	}
	
	/**
	 * Fonction qui permet d'initialiser la grille de l'ordinateur avec des bateaux al�atoires
	 */
	public static void initGrilleComparaison() {
		// D�claration des variables
		int ligne;
		int colonne;
		Direction direction;
		
		// D�claration de la variable posOk qui sert � d�terminer si le bateau peut �tre plac� � l'endroit voulu
		boolean posOk = false;
		
		// On boucle � travers la HashMap contenant l'information des bateaux
		for (Map.Entry<String, Integer> bateau : idBateaux.entrySet()) {
			do {
				// Si un nombre al�atoire entre 1 et 10 est sup�rieur � 5, la direction est horizontal
				// Il y a probablement une meilleure m�thode pour un nombre al�atoire ayant 50% de chance, mais cela fonctionne bien
				if(randRange(1, 10) > 5) {
					direction = Direction.Horizontal;
				} else { // Sinon la direction est vertical
					direction = Direction.Vertical;
				}
				
				// On obtient une position de ligne entre 0 et 9
				ligne = randRange(0, 9);
				
				// On obtient une position de colonne entre 0 et 9
				colonne = randRange(0, 9);
				
				// On regarde si la position al�atoire est valide
				posOk = PosOk(grilleOrdi, ligne, colonne, direction, grandeurBateaux.get(bateau.getValue()));
				
				// Si la position est valide, on place le bateau
				if(posOk) {
					placerBateau(grilleOrdi, ligne, colonne, direction, bateau.getValue());
				}
			} while(!posOk); // On boucle tant que la position du bateau n'est pas correct
	    }
	}
	
	/**
	 * D�claration d'un Random utilis� pour avoir des nombres al�atoires
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
	 * @param grille Grille qu'On d�sire afficher
	 */
	public static void afficherGrille(int[][] grille) {
		// Print des espaces pour l'alignement
		System.out.print("    ");
		
		// Boucle qui permet d'afficher les lettres de A � I
		for(int i = 0; i < 10; i++) {
			System.out.print(Character.toString(i + 65) + "  ");
		}
		
		// Retour � la ligne (newline)
		System.out.print("\n");
		
		// Boucle � travers les lignes
		for(int i = 0; i < 10; i++) {
			// Print le numero de ligne
			// La ligne #10 � une espace de moins pour l'alignement
			System.out.print(i + 1 + (i == 9 ? "  " : "   "));
			
			// Boucle � travers les colonnes
			for(int j = 0; j < 10; j++) {
				// Print le contenu de la grille
				System.out.print(grille[i][j] + "  ");
			}
			
			// Retour � la ligne (newline)
			System.out.print("\n");
		}
	}
	
	/**
	 * BufferedReader qui est utilis� pour lire les touches du clavier
	 */
	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	/**
	 * Fonction qui permet d'obtenir une chaine de caract�re tap�e au clavier
	 * @return La String tap�e au clavier
	 */
	public static String readString() {
		// D�claration de la variable de la String
		String res = "";
		
		try { // On tente de lire le contenu du BufferedReader
			res = br.readLine();
		} catch(Exception e) { // Si il y a un probl�me (exception) on le dit au joueur
			System.out.println("Probl�me avec le read");
		}
		
		// Retourne la String tap�e au clavier
		return res;
	}
	
	/**
	 * Fonction qui permet de v�rifier sur une String est un nombre
	 * @param s String qu'on veut v�rifier si il s'agit d'un nombre
	 * @return Si la String est un nombre, return true, sinon return false
	 */
	public static boolean isInt(String s) {
		// Retourne si la String est un nombre
		return s.matches("\\d+");
	}
	
	/**
	 * Fonction qui permet de lire un nombre � l'aide du clavier
	 * @return Le nombre lu
	 */
	public static int readInt() {
		// Demande une chaine de caract�re tant qu'il ne s'agit pas d'un nombre
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
