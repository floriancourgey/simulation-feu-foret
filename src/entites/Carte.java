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
 * FICHIER : Carte.java
 * 
 * la Carte regroupe tous les éléments du package 'entités'
 * 
 * et les lie par intercations communes
 * 
 * aucune méthode n'est vraiment principale puisqu'elle contient surtout des appels à des méthodes d'attributs
 * 
 */

package entites;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import tout.Fenetre;

public class Carte extends JPanel implements Serializable {
	private static final long serialVersionUID = -8382405646124929301L;
	
	public enum Vegetation{
		CLAIRESEMEE(50, "Clairesemée"),
		ESPACEE(75, "Espacée"),
		TOUFFUE(90, "Touffue"),
		CONTINUE(100, "Continue");
		
		private int pourcentage;
		private String nom;
		
		private Vegetation(int i, String s){
			pourcentage = i;
			nom = s;
		}
		public int getPourcentage(){
			return pourcentage;
		}
		public String toString(){
			return nom+" : "+pourcentage+"%";
		}
	}
	// vegetation de la carte
	private Vegetation vegetation;
	
	public enum Humidite{
		HUMIDE(10, "Humide"),
		NORMAL(35, "Normal"),
		SEC(60, "Sec"),
		TRES_SEC(90, "Très sec");
		
		private int pourcentage;
		private String nom;
		
		private Humidite(int i, String s){
			pourcentage = i;
			nom = s;
		}
		public int getPourcentage(){
			return pourcentage;
		}
		public String toString(){
			return nom+" ("+pourcentage+")";
		}
	}
	private Humidite humidite;
	
	// nombre de cases en hauteur de la carte
	private int hauteur = 50;
	// nombre de cases en largeur de la carte
	private int largeur = 50;
	
	// la carte est composée de la grille par dessus laquelle on appose un panel transparent pour les extincteurs
	private JPanel pGrille;
	private JPanel panelTransparent;
	
	// Matrice de Jlabel
	private Case[][] grille;
	
	// pour retenir les cases "vient_de"
	// voir Case.java pour plus de détails
	private ArrayList<Point> memoireCasesVientDe;
	
	// liste d'extincteurs
	private ArrayList<Extincteur> listeExtincteur;

	// je veux ajouter la case à dessiner sous le curseur de la souris pour voir ce que je dessine
	private boolean modeDessin;
	protected JLabel lCaseADessiner;
	public JLabel getLCaseADessiner(){
		return lCaseADessiner;
	}
	
	protected Vent vent = null;			
	
	private Heightmap heightmap = null;
	
	
	private void initDebut(){		
		// taille
		setPreferredSize(new Dimension(1000, Fenetre.HAUTEUR));
	}
	
	private void initFin(){
		// vent
		vent = new Vent(this);		
		this.add(vent,0);
		
		// init liste extincteur
		listeExtincteur = new ArrayList<Extincteur>();
		
		// l'overlayLayout permet de superposer des panels sur une pile de panels
		this.setLayout(new OverlayLayout(this));
		// /!\ l'ajout avec add se fait en dessous et non au dessus, attention ! 
		panelTransparent = new JPanel();
		panelTransparent.setOpaque(false);
		panelTransparent.setVisible(true);
		panelTransparent.setLayout(null);
		// gestion du déplacement
		chargerMotionListener();
		// gestion du clic
		chargerClickListener();
		this.add(panelTransparent);
		
		
		// initialisation de la hashmap
		memoireCasesVientDe = new ArrayList<Point>();
		
		// case à dessiner
		lCaseADessiner = new JLabel();
		lCaseADessiner.setBackground(Case.cForet);
		lCaseADessiner.setVisible(false);
		lCaseADessiner.setOpaque(true);
		lCaseADessiner.setPreferredSize(new Dimension(100,100));
		lCaseADessiner.setLocation(100,100);
		lCaseADessiner.setSize(lCaseADessiner.getPreferredSize());
		panelTransparent.add(lCaseADessiner);
		
		this.add(pGrille);
		
		setVisible(true);
		setOpaque(true);
	}

	
	
	/**
	 * Constructeur d'une nouvelle carte
	 * 
	 * "nouvelle" car il y a deux types de cartes :
	 * . les nouvelles cartes d'un nouveau jeu
	 * . les cartes de l'historique (carteBackup) pour retour arrière
	 * 
	 * Chacune des deux à son constructeur
	 * 
	 * Ici c'est le constructeur de la première catégorie
	 * 
	 * Les deux constructeurs sont structurés comme suit :
	 * 
	 * partie commune "initDebut"
	 * partie non commune
	 * partie commune "initFin"
	 * 
	 * @param v	la végétation de la carte
	 * @param zoomHeightmap	le zoom pour la h map
	 * @param h	l'humidté de la carte
	 * @param vLargeur	le nombre de cases en largeur
	 * @param vHauteur le nombre de cases en hauteur
	 */
	public Carte(Vegetation v, Integer zoomHeightmap, Humidite h, Integer vLargeur, Integer vHauteur){
		// appel du constructeur JPanel
		super();
		
		initDebut();
		
		//
		// début de la partie non commune
		//
		
		// végétation
		vegetation = v;	
		humidite = h;
		
		// initialisation de la grille de cases
		this.hauteur = vHauteur;
		this.largeur = vLargeur;
		grille = new Case[hauteur][largeur];
		
		// et de leur panel
		pGrille = new JPanel();
		pGrille.setLayout(new GridLayout(hauteur, largeur));
		
		// heightmap
		Float zoom = new Float ((zoomHeightmap == null) ? Heightmap.DEFAULT_ZOOM : zoomHeightmap);
		heightmap = new Heightmap(this, zoom, vegetation);
		this.add(heightmap,0);
		
		// init des cases
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<largeur ; j++){
				Case c = new Case(vegetation.getPourcentage(), i, j, heightmap.getHeightMapNature()[i][j]);	
				c.setVisible(true);
				pGrille.add(c);
				grille[i][j] = c;
			}
		}
		
		// on peut désormais supprimer la heightmap
		this.remove(heightmap);
		
		//
		// fin partie non commune
		//
		
		initFin();
	}
	
	/**
	 * Constructeur d'une carte à partir d'une carte
	 * ( = construction d'un clone )
	 * 
	 * "nouvelle" car il y a deux types de cartes :
	 * . les nouvelles cartes d'un nouveau jeu
	 * . les cartes de l'historique (carteBackup) pour retour arrière
	 * 
	 * Chacune des deux à son consctructeur
	 * 
	 * Ici c'est le constructeur de la seconde catégorie
	 * 
	 * Les deux constructeurs sont structurés comme suit :
	 * 
	 * partie commune "initDebut"
	 * partie non commune
	 * partie commune "initFin"
	 * 
	 * @param carteCopie
	 */
	public Carte(Carte carteCopie){
		super();
		
		initDebut();
		
		//
		// début de la partie non commune
		//
		
		// végétation
		vegetation = carteCopie.getVegetation();
		humidite = carteCopie.getHumidite();
		
		// initialisation de la grille de cases
		this.hauteur = carteCopie.getHauteur();
		this.largeur = carteCopie.getLargeur();
		
		grille = new Case[this.hauteur][this.largeur];
		
		// et de leur panel
		pGrille = new JPanel();
		pGrille.setLayout(new GridLayout(hauteur, largeur));
		
		// init des cases
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<largeur ; j++){
				Case c = new Case(carteCopie.getGrille()[i][j]);
				c.setVisible(true);
				pGrille.add(c);
				grille[i][j] = c;
			}
		}
		
		//
		// fin partie non commune
		//
		
		initFin();
	}
	
	public void chargerClickListener(){
		panelTransparent.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
				if(Fenetre.getCbDessiner().isSelected()){
					dessin(e);
				} else {
					getCase(e).fire();
				}
				
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
	
	public void chargerMotionListener(){
		panelTransparent.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				actionCommune(e);
			}
			
			public void mouseDragged(MouseEvent e){
				actionCommune(e);
				if(SwingUtilities.isLeftMouseButton(e)){
					if(Fenetre.getCbDessiner().isSelected()){
						dessin(e);
					}
				}
			}
			
			private void actionCommune(MouseEvent e){
				if(modeDessin){
		        	lCaseADessiner.setLocation(e.getX()-lCaseADessiner.getWidth()/2, e.getY()-lCaseADessiner.getHeight()/2);
		    		lCaseADessiner.setVisible(true);
		    	} else {
		    		lCaseADessiner.setVisible(false);
		    		panelTransparent.repaint();
		    	}
			}
		});
	}
	
	public Case[][] getGrille(){
		return grille;
	}
	
	public void remiseAZero(){
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<largeur ; j++){
				grille[i][j].remiseAZero();
			}
		}
	}
	
	public void redessiner(){
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<largeur ; j++){
				grille[i][j].redessiner();
			}
		}
	}
	
	@Override
	public String toString(){
		String s = "";
		String separateur = "|";
		String separateurCombustionGauche = "(";
		String separateurCombustionDroite = ")";
		for(int i=0 ; i<hauteur ; i++){
			System.out.print(separateur);
			for(int j=0 ; j<largeur ; j++){
				System.out.print(grille[i][j].getNatureString());
				System.out.print(separateurCombustionGauche);
				System.out.print(grille[i][j].getCombustionString());
				System.out.print(separateurCombustionDroite);
				System.out.print(separateur);
			}
			System.out.println();
		}
		return s;
	}

	public ArrayList<Point> getMemoireCasesVientDe() {
		return memoireCasesVientDe;
	}

	public void setMemoireCasesVientDe(ArrayList<Point> memoireCasesVientDe) {
		this.memoireCasesVientDe = memoireCasesVientDe;
	}

	public void ajoutExtincteur() {
		// on choisit aléatoirement la position de l'extincteur
		// avec une condition : on se laisse une marge de 3 cases de chaque côté sur laquelle il ne peut y avoir d'extincteur
		final int MARGE = 3;
		int x = (int)(Math.random()*(largeur-2*MARGE))+MARGE;
		int y = (int)(Math.random()*(hauteur-2*MARGE))+MARGE;
		
		// on le crée
		Extincteur e = new Extincteur(grille[y][x], panelTransparent, grille);
		
		// on l'ajoute au panel
		panelTransparent.add(e);
		this.repaint();
		// et à la l'arraylist
		listeExtincteur.add(e);
	}
	
	public Vegetation getVegetation(){
		return vegetation;
	}
	
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
		Carte other = (Carte) obj;
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<largeur ; j++){
				if(!grille[i][j].equals(other.getGrille()[i][j])){
					return false;
				}
			}
		}
		return true;
	}
	
	public void definirPositionExtincteurs(){
		if(!listeExtincteur.isEmpty()){
			for(Extincteur e : listeExtincteur){
				e.definirPosition();
			}
		}
		repaint();
	}
	
	public ArrayList<Extincteur> getListeExtincteur(){
//		return null;
		return listeExtincteur;
	}
	
	public JPanel getPanelTransparent(){
		return panelTransparent;
	}
	
	public boolean isModeDessin() {
		return modeDessin;
	}

	public void setModeDessin(boolean modeDessin) {
		this.modeDessin = modeDessin;
	}
	
	protected Case getCase(MouseEvent e){
		int ligne = (int)(e.getY()/grille[0][0].getHeight());
		int colonne = (int)(e.getX()/grille[0][0].getWidth());
		if(ligne < 0){
			ligne = 0;
		} else if(ligne >= hauteur){
			ligne = hauteur-1;
		}
		if(colonne < 0){
			colonne = 0;
		} else if(colonne >= largeur){
			colonne = largeur-1;
		}
		Point p = new Point(colonne,ligne);
		return grille[p.y][p.x];
	}
	
	public void toggleVent(){
		vent.setVisible(!vent.isVisible());
	}
	
	protected void dessin(MouseEvent e){
		Case caseSelectionnee = getCase(e);
		final int rayon = (int)Fenetre.getCbNbCaseAColorier().getSelectedItem();
		for(int i=-(rayon/2) ; i<(rayon/2)+1 ; i++){
			for(int j=-(rayon/2) ; j<(rayon/2)+1 ; j++){
				int ligne = caseSelectionnee.getLigne()+i;
				int colonne = caseSelectionnee.getColonne()+j;
				if(ligne<0 || ligne>=hauteur || colonne<0 || colonne>=largeur){
					continue;
				}
				grille[ligne][colonne].dessin();
			}
		}
	}
	
	public void actualiserCouleurCaseADessiner(Color couleur){
		lCaseADessiner.setBackground(couleur);
	}

	public Vent getVent() {
		return vent;
	}

	public void setVent(Vent vent) {
		this.vent = vent;
	}

	public Humidite getHumidite() {
		return humidite;
	}

	public void setHumidite(Humidite humidite) {
		this.humidite = humidite;
	}

	public int getHauteur() {
		return hauteur;
	}

	public void setHauteur(int hauteur) {
		this.hauteur = hauteur;
	}

	public int getLargeur() {
		return largeur;
	}

	public void setLargeur(int largeur) {
		this.largeur = largeur;
	} 
}
