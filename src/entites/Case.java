/**
 * 
 * PROJET : Simulation d'un feu de forêt JAVA
 * AUTEURS : COURGEY Florian - GUÉNARD Thomas
 * ANNÉE : 2014
 * ÉCOLE : EPF École d'ingénieurs
 * 
 * Fonctionnalités (non ordonnées) :
 * - terrain généré par heightmap
 * - Extincteur pour éteindre le feu
 * - historique de modifications avec retour arrière (comme un CTRL Z)
 * - dessin simple mais avancé
 * - pas d'horloge au clic ou au temps
 * - sauvegarde/chargement carte
 * - vent paramétrable en intensité et direction
 * 
 * ORGANISATION :
 * le fichier Main.java contient le main qui lance uniquement une nouvelle Fenetre de Fenetre.java
 * la Fenetre est l'unique JFrame du programme, tout se passe dedans
 * Elle contient surtout la Carte de Carte.java
 * et cette Carte fait appel à toutes les fonctionnalités puisqu'elle contient
 * une matrice de Case de Case.java
 * une Heightmap de Heightmap.java
 * un Vent de Vent.java
 * des Extincteurs de Extincteur.java
 * 
 */

/**
 * 
 * FICHIER : Case.java
 * 
 * les objets Case sont les unités de base du programme * 
 * 
 * méthodes principales : 
 * 
 * dessin qui dessine la case selon les paramètres du jpanel pParamètresDessin de Fenetre
 * fire qui met le feu à la case si elle est valide
 * combustion qui gère les stades de combustion de la case
 */

package entites;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import tout.Fenetre;

public class Case extends JLabel implements Serializable {
	private static final long serialVersionUID = -5462486194970405468L;
	
	public enum Nature{
		MAISON(8),
		FORET(4),
		PLAINE(2),
		EAU(0),
		CHEMIN(0);
		
		private int iteration;
		
		private Nature(int i){
			iteration = i;
		}
		
		public int getIteration(){
			return iteration;
		}
	};
	
	public enum Combustion{
		RIEN,
		// "vient_de" sera expliqué plus bas
		VIENT_DE,
		EN_FEU,
		BRULEE_CHAUD,
		BRULEE_FROID,
		CENDRE;
	};
	
	// comme nous parcourons 1 seule fois notre grille
	// il faut éviter le cas où
	// une case enflammée, nommée A,enflamme sa case immédiate droite, que l'on va appeller B
	// puis l'automate passe à la case suivante : B
	// il détecte qu'elle est enflammée
	// et lui fait subir un premier tour d'inflammation
	// ce qui est une erreur
	//
	// pour y remédier, on ajout un état :
	// vient tout juste de se faire enflammer, appelé "vient_de"
	//
	// ainsi, A enflamme B en le passant dans cet état
	// l'automate arrive sur B, voit que c'est du "vient-de", donc passe à autre chose
	// à la fin du tour, on passe tous les états "vient_de" à l'état "en_feu"
	// pour qu'au tour suivant l'automate commence l'inflammation de B
	//
	// optimisation :
	// pour éviter à la fin de chaque tour de parcourir toute la carte à la recherche de "vient_de"
	// on ajoute à la classe Carte une liste de Point (car le Point est un couple d'entiers, exactement ce que nous cherchons !!)
	// le x sera la colonne de la case
	// la y sera la ligne de la case
	// on a donc "ArrayList<Point> memoire"
	// qui sera utilisé comme suit :
	// int colonne = memoire.getX
	// int ligne = memoire.getY
	
	
	// la nature est initialisée pour éviter un Null pointer lors du equals
	private Nature natureCaseADessiner = Nature.CHEMIN;
	
	

	private int colonne;
	private int ligne;
	
	
	// c pour COULEUR
	public static final Color cMaison = Color.MAGENTA;
	public static final Color cForet = Color.GREEN;
	public static final Color cPlaine = Color.YELLOW;
	public static final Color cEau = Color.BLUE;
	public static final Color cChemin = Color.GRAY;
	
	// p pour PROBABILITÉS
	public static final double P_BRULEE_FROID = 0.40;
	
	public static final int NB_ITERATION_BRULEE_FROID = 3;
	
	
	// valeurs par défaut pour remise à zéro
	public static final int DEFAUT_NB_ITERATION_BRULEE_FROID = 0;
	public static final Combustion DEFAUT_COMBUSTION = Combustion.RIEN;
	public static final int DEFAUT_NB_FEU = 0;
	
	// paramètres de la case
	// nature de la case : maison, foret, plaine
	private Nature nature;
	// nombre d'itérations de feu sur cette case
	private Integer nbFeu = DEFAUT_NB_FEU;
	// etat de combustion de la case
	private Combustion combustion = DEFAUT_COMBUSTION;
	// nombre d'itérations que la case a subi en brulée froid
	private int nbIterationEnBruleeFroid = DEFAUT_NB_ITERATION_BRULEE_FROID;
	
	
	public final Case moi = this;

	protected Color couleurDeBase;

	public Case(int pourcentage_inflammable, int ligne, int colonne, Nature nature){
		super();
		this.ligne = ligne;
		this.colonne = colonne;
		// nb Feu
//		this.setText(nbFeu.toString());
		//bordure 
		setBorder(BorderFactory.createLineBorder(Color.black));
		// on définit ici le terrain
		this.nature = nature;
//		double rand = Math.random();
//		double pourcentage = (double)pourcentage_inflammable;
//		if(rand < pourcentage/100){
//			// on tire au hasard entre une maison, une foret ou une plaine
//			rand = Math.random();
//			if(rand < 0.33){
//				setNature(Nature.MAISON);
//			} else if (rand < 0.66){
//				setNature(Nature.FORET);
//			}else{
//				setNature(Nature.PLAINE);
//			}
//		} else {
//			// on tire au hasard entre un chemin et de l'eau
//			rand = Math.random();
//			if(rand < 0.5){
//				setNature(Nature.EAU);
//			}else{
//				setNature(Nature.CHEMIN);
//			}
//		}
		this.couleurDeBase = dessinerBackground();
		// il est obligatoire de rendre le background opaque
		// sans la prochaine ligne, il est invisible
		setOpaque(true);
	}
	
	/**
	 * pour la copie
	 * @param c
	 */
	public Case(Case c){
		super();
		this.nature = c.getNature();
		this.combustion = c.getCombustion();
		this.nbFeu = c.getNbFeu();
		this.nbIterationEnBruleeFroid = c.getNbIterationEnBruleeFroid();
		this.couleurDeBase = c.getMemoireCouleur();
		this.ligne = c.getLigne();
		this.colonne = c.getColonne();
		// nb Feu
//		this.setText(nbFeu.toString());
		//bordure 
		setBorder(BorderFactory.createLineBorder(Color.black));
		setOpaque(true);
	}
	
	public void dessin(){
//		if(Fenetre.getRbForet().isSelected()){
//			natureCaseADessiner = Nature.FORET;
//		} else if(Fenetre.getRbPlaine().isSelected()){
//			natureCaseADessiner = Nature.PLAINE;
//		}
		nature = (Nature)Fenetre.getCobDessiner().getSelectedItem();
		couleurDeBase = getColor(nature);
//		redessiner();
		remiseAZero();
	}
	
	public void fire(){
		// si la case n'est pas inflammable, on quitte
		if(		nature == Nature.EAU 
			|| 	nature == Nature.CHEMIN
			|| 	combustion != Combustion.RIEN){
			JOptionPane.showMessageDialog(moi,"Vous venez de cliquer sur une case qui ne peut pas prendre feu.\nC'est impossible ! Merci de cliquer ailleurs.",
					"Pas de feu sur les cases ininflammables !", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// si l'animation n'est pas déjà lancée, on lance
//		if(Fenetre.nbIteration == 0){
			metsLeFeu();
			Fenetre.ajoutIteration(true);
//		}
	}
	
	private Color dessinerBackground() {
		Color c = getColor(getNature());
		this.setBackground(c);
		return c;
	}

	
	public static Color getColor(Nature n){
		Color c = null;
		switch(n){
		case MAISON :
			c = cMaison;
			break;
		case FORET :
			c = cForet;
			break;
		case PLAINE :
			c = cPlaine;
			break;
		case CHEMIN:
			c = cChemin;
			break;
		case EAU:
			c = cEau;
			break;
		}	
		return c;
	}
	


	public void combustion() {
		switch(combustion){
		case RIEN :
			break;
		case VIENT_DE :
			break;
		case EN_FEU :
			// on augmente le nombre de fois que la case a brulé
			nbFeu++;
//			setText(nbFeu.toString());
			// si ce nombre vaut la limite de son état, elle passe en brulé chaud
			if(nbFeu == getNature().getIteration()){
				metsLeBrulerChaud();
			}
			break;
		case BRULEE_CHAUD :
			nbFeu++;
//			setText(nbFeu.toString());
			double rand = Math.random();
			if(rand < P_BRULEE_FROID){
				metsLeBrulerFroid();
			}
			break;
		case BRULEE_FROID :
			nbFeu++;
//			setText(nbFeu.toString());
			nbIterationEnBruleeFroid++;
			if(nbIterationEnBruleeFroid >= NB_ITERATION_BRULEE_FROID){
				metsLeEnCendres();
			}
			break;
		case CENDRE :
			break;
		}
		
	}
	
	public String getCombustionString(){
		String s = "";
		switch(combustion){
		case RIEN :
			s = "r";
			break;
		case VIENT_DE :
			s = "vd";
			break;
		case EN_FEU :
			s = "f";
			break;
		case BRULEE_CHAUD :
			s = "bc";
			break;
		case BRULEE_FROID :
			s = "bf";
			break;
		case CENDRE :
			s = "c";
			break;
		}
		return s;
	}
	
	public String getNatureString(){
		String s = "";
		switch(nature){
		case FORET :
			s = "F";
			break;
		case MAISON :
			s = "M";
			break;
		case PLAINE :
			s = "P";
			break;
		case CHEMIN:
			s = "C";
			break;
		case EAU:
			s = "E";
			break;
		}
		return s;
	}
	
	public void metsLeFeu(){
		combustion = Combustion.EN_FEU;
		setBackground(Color.ORANGE);
//		java.net.URL imgUrl2 = getClass().getResource("/images/en_feu.jpg");
//		setIcon(new ImageIcon(new ImageIcon(imgUrl2).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
	}
	
	public void metsLeVientDe(){
		combustion = Combustion.VIENT_DE;
//		setText(getText()+" VIENT_DE");
	}
	
	public void metsLeBrulerChaud(){
		combustion = Combustion.BRULEE_CHAUD;
		setBackground(Color.red);
	}
	
	public void metsLeBrulerFroid(){
		combustion = Combustion.BRULEE_FROID;
		setBackground(Color.cyan);
	}
	
	public void metsLeRien(){
		combustion = Combustion.RIEN;
		setBackground(couleurDeBase);
	}
	

	public void metsLeEnCendres(){
		combustion = Combustion.CENDRE;
		setBackground(Color.black);
	}
	
	public Integer getNbFeu(){
		return nbFeu;
	}
	
	public Combustion getCombustion(){
		return combustion;
	}
	
	public void remiseAZero(){
		nbIterationEnBruleeFroid = DEFAUT_NB_ITERATION_BRULEE_FROID;
		combustion = DEFAUT_COMBUSTION;
		nbFeu = DEFAUT_NB_FEU;
//		setText(nbFeu.toString());
		metsLeRien();
	}
	
	public void redessiner(){
		switch(combustion){
		case RIEN :
			metsLeRien();
			dessinerBackground();
			break;
		case BRULEE_CHAUD :
			metsLeBrulerChaud();
			break;
		case BRULEE_FROID :
			metsLeBrulerFroid();
			break;
		case EN_FEU :
			metsLeFeu();
			break;
		case CENDRE :
			metsLeEnCendres();
			break;
		case VIENT_DE:
			metsLeRien();
			break;
		}
//		setText(nbFeu.toString());
	}

	public Nature getNature() {
		return nature;
	}

	public void setNature(Nature nature) {
		this.nature = nature;
	}
	
	public void setCombustion(Combustion c){
		combustion = c;
	}
	
	public int getNbIterationEnBruleeFroid() {
		return nbIterationEnBruleeFroid;
	}

	public void setNbIterationEnBruleeFroid(int nbIterationEnBruleeFroid) {
		this.nbIterationEnBruleeFroid = nbIterationEnBruleeFroid;
	}
	

	public Color getMemoireCouleur() {
		return couleurDeBase;
	}

	public void setMemoireCouleur(Color memoireCouleur) {
		this.couleurDeBase = memoireCouleur;
	}
	
	public int getColonne() {
		return colonne;
	}
	public void setColonne(int colonne) {
		this.colonne = colonne;
	}
	public int getLigne() {
		return ligne;
	}
	public void setLigne(int ligne) {
		this.ligne = ligne;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Case other = (Case) obj;
		return 
				natureCaseADessiner.equals(other.natureCaseADessiner) && 
				colonne ==other.colonne && 
				ligne ==other.ligne && 
				nature.equals(other.nature) && 
				nbFeu.equals(other.nbFeu) && 
				combustion.equals(other.combustion) && 
				nbIterationEnBruleeFroid == other.nbIterationEnBruleeFroid && 
				couleurDeBase.equals(other.couleurDeBase)
				;
	}
}
