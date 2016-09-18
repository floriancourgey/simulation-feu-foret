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
 * FICHIER : Fenetre.java
 * 
 * Fichier très dense car contient tous les JComponent (difficile à fragmenter en plusieurs fichiers car beaucoup d'inter dépendances, cela nuirait à la facilité de lecture)
 * La partie principale se trouve dans ajoutIteration où l'on trouve l'algorithme principal
 *
 * 
 * Tout le jeu se déroule dans une seule et même fenêtre, celle ci.
 * 
 * Elle est organisée en deux parties (panel pGlobal) :
 * 
 * . celle de gauche qui contient les paramètres (généraux, de la carte, de dessin)
 *   (panel pParametres)
 * 
 * . celle de droite avec uniquement la carte
 *   (panel pCarte)
 *   
 *   Il est recommandé de cacher avec le petit moins les méthodes :
		initFrame();
		initInterface();
		
	pour plus de clarté
 */


package tout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import entites.Carte;
import entites.Carte.Humidite;
import entites.Carte.Vegetation;
import entites.Case;
import entites.Case.Combustion;
import entites.Case.Nature;


public class Fenetre extends JFrame {
	private static final long serialVersionUID = 8164118974463460991L;
	
	// titre de la fenêtre globale
	public static final String TITRE = "Simulation d'un feu de forêt";
	
	public static final int HAUTEUR = 800;
	public static final int LARGEUR = 1000;
	public static final int LARGEUR_PARAMETRES = 500;
	
	public static final int LARGEUR_GRILLE_DEFAUT = 50;
	public static final int HAUTEUR_GRILLE_DEFAUT = 50;
	
	// création d'un classe Jpanel perso pour éviter de réécrire les choses 4 fois..
	private class JPanelEtched extends JPanel{
		private static final long serialVersionUID = 3071079089708734992L;
		public JPanelEtched(String entete, int lignes){
			super();
			Border b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
			this.setBorder(b);
			this.setLayout(new FlowLayout());
//			this.setLayout(new GridLayout(lignes, 1));
//			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			JLabel en_tete = new JLabel(entete, JLabel.CENTER);
			en_tete.setFont(new Font("Serif", Font.BOLD, 48));
			this.setMinimumSize(new Dimension(LARGEUR_PARAMETRES/2, HAUTEUR));
			this.setPreferredSize(new Dimension(LARGEUR_PARAMETRES, HAUTEUR));
			// top, gauche, bas, droite
			Border padding = BorderFactory.createEmptyBorder(0,10,0,10);
			Border border_gauche = BorderFactory.createMatteBorder(0, 15, 0, 0, Color.BLACK);
			Border border_bas = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.RED);
			CompoundBorder border = new CompoundBorder(border_gauche, border_bas);
			en_tete.setBorder(BorderFactory.createCompoundBorder(border,padding));
			this.add(en_tete);
		}
	}
	
	// panels
	private static JSplitPane pGlobal;
	private static Carte pCarte;
	private static JPanel pParametres;
	private static JPanelEtched pParametresGeneraux;
	private static JPanelEtched pParametresCarte;
	private static JPanelEtched pParametresDessin;
	private static JPanelEtched pParametresVent;
	
	// carte utilisée pour le retour arrière et recommencer
	private static ArrayList<Carte> carteBackup;
	
	// composants paramètres
	public static JLabel lGo;
	private static JButton bProchaineIteration;
	private static JLabel lNbIteration;
	private static JButton bRecommencer;
	private static JButton bNouveau;
	private static JButton bRetourArriere;
	private static JCheckBox cbDessiner;
	public static JCheckBox getCbDessiner() {
		return cbDessiner;
	}
	private static JComboBox<Nature> cobDessiner;
	public static JComboBox<Nature> getCobDessiner() {
		return cobDessiner;
	}
	private static JButton bSauvegarder;
	private static JButton bCharger;
	private static JComboBox<Integer> cbNbCaseAColorier;
	public static JComboBox<Integer> getCbNbCaseAColorier(){
		return cbNbCaseAColorier;
	}
	private static JLabel lVegetation = new JLabel("Végétation :");
	private static JComboBox<Vegetation> cbVegetation;
	private static JLabel lHumidite = new JLabel("Humidité :");
	private static JComboBox<Humidite> cbHumidite;
	private static JButton bAjoutExtincteur;
	private static JButton bVitessePause;
	private static JButton bVitesseNormale;
	private static JButton bVitesseVite;
	private static JButton bVitesseTresVite;
	private static int vitesse;
	public static final int VITESSE_NORMAL = 1;
	public static final int VITESSE_VITE = 3;
	public static final int VITESSE_TRESVITE = 10;
	private static ThVitesse thVitesse;
	private JLabel lIntensiteVent;
	private JSlider sIntensiteVent;
	private JLabel lDirectionVent;
	private JSlider sDirectionVent;
	private JLabel lLargeurVent;
	private JSlider sLargeurVent;
	private JCheckBox cbAfficherVent;
	
	private JLabel lZoomHeightmap;
	private JSlider sZoomHeightmap;
	private JLabel lHauteurGrille;
	private JSlider sHauteurGrille;
	private JLabel lLargeurGrille;
	private JSlider sLargeurGrille;
	
//	private static Fenetre moi = this;
	
	// nombre d'itérations totales parcourues
	public static Integer nbIteration = 0;
	
	// nombre maximum de cases pour dessiner
	public final int MAX_CASES_DESSIN = 10;
	
	public Fenetre(){
		// initialisation de la fenêtre
		initFrame();
		
		// initialisation de tout ce qu'il y a dedans
		initInterface();
		
		// test extincteur
		for(int i=0 ; i<5 ; i++){
			pCarte.ajoutExtincteur();
		}
		
//		System.out.println(pCarte.getVent().getMatrice());
	}
	
	private void initFrame(){
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setVisible(true);
		setSize(new Dimension(LARGEUR,HAUTEUR));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
//		setLocationRelativeTo(null);
		setTitle(TITRE);
	}
	
	
	private void initInterface(){
		// initialisation des panels
		pParametres = new JPanel();
		pParametres.setPreferredSize(new Dimension(500, HAUTEUR));
		pParametres.setLayout(new GridLayout(4,1));
		pParametresGeneraux = new JPanelEtched("Général", 5);
		pParametresCarte = new JPanelEtched("Carte", 5);
		pParametresDessin = new JPanelEtched("Dessin", 5);
		pParametresVent = new JPanelEtched("Vent", 5);
		pParametres.add(pParametresGeneraux);
		pParametres.add(pParametresCarte);
		pParametres.add(pParametresDessin);
		pParametres.add(pParametresVent);
		
		
		//////////////
		////////////// PANEL PARAMETRES GENERAUX
		//////////////
		// recommencer
		bRecommencer = new JButton("Recommencer");
		bRecommencer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				recommencer();
			}
		});
		pParametresGeneraux.add(bRecommencer);
		
		// nouveau jeu
		bNouveau = new JButton("Nouveau jeu");
		bNouveau.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				nouveauJeu();
			}
		});
		pParametresGeneraux.add(bNouveau);
		
		// zoom de la heightmap pour nouveau jeu
		lZoomHeightmap = new JLabel("Zoom");
		pParametresGeneraux.add(lZoomHeightmap);
		sZoomHeightmap = new JSlider(JSlider.HORIZONTAL, 2, 10, 5);
		sZoomHeightmap.setMajorTickSpacing(1);
		sZoomHeightmap.setPaintTicks(true);
		pParametresGeneraux.add(sZoomHeightmap);
		
		
		
		
		lHauteurGrille = new JLabel("Hauteur Grille");
		pParametresGeneraux.add(lHauteurGrille);
		sHauteurGrille = new JSlider(JSlider.HORIZONTAL, 5, 50, HAUTEUR_GRILLE_DEFAUT);
		pParametresGeneraux.add(sHauteurGrille);
		
		lLargeurGrille = new JLabel("Largeur Grille");
		pParametresGeneraux.add(lLargeurGrille);
		sLargeurGrille = new JSlider(JSlider.HORIZONTAL, 5, 50, LARGEUR_GRILLE_DEFAUT);
		pParametresGeneraux.add(sLargeurGrille);
		
		// sauvegarder
		bSauvegarder = new JButton("Sauvegarder");
		bSauvegarder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sauvegarder();
			}
		});
		pParametresGeneraux.add(bSauvegarder);
		
		// charger
		bCharger = new JButton("Charger");
		bCharger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				charger();
			}
		});
		pParametresGeneraux.add(bCharger);
		
		// choisir la végétation
		pParametresGeneraux.add(lVegetation);
		cbVegetation = new JComboBox<Vegetation>(Vegetation.values());
		cbVegetation.setSelectedItem(Vegetation.ESPACEE);
		pParametresGeneraux.add(cbVegetation);	
		
		// choisir l'humidité
		pParametresGeneraux.add(lHumidite);
		cbHumidite = new JComboBox<Humidite>(Humidite.values());
		cbHumidite.setSelectedItem(Humidite.NORMAL);
		pParametresGeneraux.add(cbHumidite);	
		
		
		
		//////////////
		////////////// PANEL PARAMETRES DE LA CARTE
		//////////////
		// Prochain tour d'horloge (fonction principale)
		bProchaineIteration = new JButton("Prochaine itération");
		bProchaineIteration.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				ajoutIteration(false);
			}
		});
		pParametresCarte.add(bProchaineIteration);
		
		// Revenir en arrière à la manière d'un Ctrl-Z
		bRetourArriere = new JButton("Retour arrière");
		bRetourArriere.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				retourArriere();
			}
		});
		pParametresCarte.add(bRetourArriere);
		
		// Affichage du nombre d'itérations jusqu'à lors
		lNbIteration = new JLabel();
		rafraichirLabelIteration();
		pParametresCarte.add(lNbIteration);
		
		bAjoutExtincteur = new JButton("Ajouter un extincteur");
		bAjoutExtincteur.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				pCarte.ajoutExtincteur();
			}
		});
		pParametresCarte.add(bAjoutExtincteur);
		
		thVitesse = new ThVitesse();
		thVitesse.start();
		bVitessePause = new JButton("||");
		bVitessePause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				thVitesse.pause();
			}
		});
		bVitesseNormale = new JButton(">");
		bVitesseNormale.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				vitesse = VITESSE_NORMAL;
				thVitesse.go();
			}
		});
		bVitesseVite = new JButton(">>");
		bVitesseVite.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				vitesse = VITESSE_VITE;
				thVitesse.go();
			}
		});
		bVitesseTresVite = new JButton(">>>");
		bVitesseTresVite.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				vitesse = VITESSE_TRESVITE;
				thVitesse.go();
			}
		});
		pParametresCarte.add(bVitessePause);
		pParametresCarte.add(bVitesseNormale);
		pParametresCarte.add(bVitesseVite);
		pParametresCarte.add(bVitesseTresVite);
		
		
		
		//////////////
		////////////// PANEL PARAMETRES DE DESSIN
		//////////////
		cbDessiner = new JCheckBox("Dessiner");
		cbDessiner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(getCbDessiner().isSelected()){
					pCarte.actualiserCouleurCaseADessiner(Case.getColor(((Nature)(getCobDessiner().getSelectedItem()))));
					pCarte.setModeDessin(true);
				} else {
					pCarte.setModeDessin(false);
				}
			}
		});
		pParametresDessin.add(cbDessiner);
		
		cobDessiner = new JComboBox<Case.Nature>(Nature.values());
		cobDessiner.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				pCarte.actualiserCouleurCaseADessiner(Case.getColor(((Nature)(getCobDessiner().getSelectedItem()))));
			}
		});
		pParametresDessin.add(cobDessiner);
		
		Integer tab[] = new Integer[MAX_CASES_DESSIN/2];
		int compteur = 1;
		for(int i=0 ; i<MAX_CASES_DESSIN/2 ; i++){
			tab[i] = compteur;
			compteur += 2;
		}
		cbNbCaseAColorier = new JComboBox<Integer>(tab);
		// quand on change le rayon de cases à colorier, on va changer la taille du label
		cbNbCaseAColorier.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int rayon = (int)cbNbCaseAColorier.getSelectedItem();
				pCarte.getLCaseADessiner().setSize(rayon*pCarte.getGrille()[0][0].getWidth(),rayon*pCarte.getGrille()[0][0].getHeight());
			}
		});
		pParametresDessin.add(cbNbCaseAColorier);
		
		// CARTE
		pCarte = new Carte((Vegetation)cbVegetation.getSelectedItem(), null, (Humidite)cbHumidite.getSelectedItem(), sLargeurGrille.getValue(), sHauteurGrille.getValue());
//		pCarte = new Carte((Vegetation)cbVegetation.getSelectedItem(), null, sLargeurGrille.getValue(), sHauteurGrille.getValue());
		
		//////////////
		////////////// PANEL PARAMETRES DU VENT
		//////////////
		lIntensiteVent = new JLabel("Intensité");
		pParametresVent.add(lIntensiteVent);
		sIntensiteVent = new JSlider(JSlider.HORIZONTAL, 10, 30, 20);
		sIntensiteVent.setMajorTickSpacing(10);
		sIntensiteVent.setMinorTickSpacing(2);
		sIntensiteVent.setPaintTicks(true);
		sIntensiteVent.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
		        int intensite = (int)source.getValue();
		        pCarte.getVent().setIntensite(intensite);
		        pCarte.getVent().repaint();
			}
		});
		pParametresVent.add(sIntensiteVent);
		
		lDirectionVent = new JLabel("Direction");
		pParametresVent.add(lDirectionVent);
		sDirectionVent = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
		sDirectionVent.setMajorTickSpacing(60);
		sDirectionVent.setMinorTickSpacing(10);
		sDirectionVent.setPaintTicks(true);
		sDirectionVent.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
		        int direction = (int)source.getValue();
		        pCarte.getVent().setDirection(direction);
		        pCarte.getVent().repaint();
			}
		});
		pParametresVent.add(sDirectionVent);
		
		lLargeurVent = new JLabel("Largeur");
		pParametresVent.add(lLargeurVent);
		sLargeurVent = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
		sLargeurVent.setMajorTickSpacing(1);
		sLargeurVent.setPaintTicks(true);
		sLargeurVent.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
		        int largeur = (int)source.getValue();
		        pCarte.getVent().setLargeur(largeur);
		        pCarte.getVent().repaint();
			}
		});
		pParametresVent.add(sLargeurVent);
		
		cbAfficherVent = new JCheckBox("Afficher vent");
		cbAfficherVent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pCarte.toggleVent();
			}
		});
		pParametresVent.add(cbAfficherVent);
//		pParametresVent.add(pCarte.getVent());
		
		
		// Carte backup
		carteBackup = new ArrayList<Carte>();
		carteBackup.add(new Carte(pCarte));
		
		
		// initialisation du panel Global
		pGlobal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				pParametres, pCarte);
		// mettre la carte en grand d'un clic
		pGlobal.setOneTouchExpandable(true);
		// définir la position par défaut du divider
		pGlobal.setDividerLocation(LARGEUR_PARAMETRES);
		this.setContentPane(pGlobal);
		
		
		// à laisser en fin de méthode
		pack();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	private void recommencer(){
		cbAfficherVent.setSelected(false);
		nbIteration = 0;
		rafraichirLabelIteration();
		pGlobal.remove(pCarte);
		pCarte.remiseAZero();
		pGlobal.add(pCarte);
	}
	private void nouveauJeu(){
		cbAfficherVent.setSelected(false);
		nbIteration = 0;
		rafraichirLabelIteration();
		pGlobal.remove(pCarte);
		pCarte = new Carte((Vegetation)cbVegetation.getSelectedItem(), sZoomHeightmap.getValue(), (Humidite)cbHumidite.getSelectedItem(), sLargeurGrille.getValue(), sHauteurGrille.getValue());
		pGlobal.add(pCarte);
		actualiserCarte();
	}
	
	private void retourArriere(){
		if(carteBackup.size() > 1 && nbIteration > 0){
			nbIteration--;
			rafraichirLabelIteration();
			
			pGlobal.remove(pCarte);
			
			carteBackup.remove(carteBackup.size()-1);
			pCarte = new Carte(carteBackup.get(carteBackup.size()-1));
			
			pCarte.redessiner();
			
			pGlobal.add(pCarte);
			
			pGlobal.revalidate();
			pCarte.revalidate();
			pGlobal.repaint();
			pCarte.repaint();
		} else {
			JOptionPane.showMessageDialog(this, "Impossible de retourner en arrière");
		}
	}
	
	private void charger(){
		pGlobal.remove(pCarte);
		try {
			FileInputStream fis = new FileInputStream("test");
			ObjectInputStream in = new ObjectInputStream(fis);
			pCarte = (Carte)in.readObject();
			in.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		pCarte.chargerClickListener();
		pCarte.chargerMotionListener();
		pGlobal.add(pCarte);
		actualiserCarte();
	}
	
	private void sauvegarder(){
		try {
	         FileOutputStream fos = new FileOutputStream("test");
	         ObjectOutputStream out = new ObjectOutputStream(fos);
	         out.writeObject(pCarte);
	         out.flush();
	         out.close();
	      }
	      catch (IOException e) {
	          System.out.println(e); 
	      }
	}
		
	private static void rafraichirLabelIteration(){
		lNbIteration.setText("Nombre d'itérations : "+nbIteration.toString());
	}
	
	/**
	 * lance l'algorithme principal de parcours de toutes les cases
	 * et de leur gestion en fonction de leur combustion, du vent, etc
	 * 
	 * @param firstTime ce paramètre est un petit correctif.
	 * Il vaut vrai quand on vient tout juste de cliquer pour lancer un feu.
	 * L'algo va ainsi faire qu'une moitié du traitement :
	 * on a juste besoin qu'il incrémente deux-trois variables mais pas qu'il parcourt toutes les cases pour les mettre à jour
	 */
	public static void ajoutIteration(boolean firstTime){
		
		// on augmente le nb d'itérations global de la carte
		nbIteration++;
		// on met à jour le label qui nous indique cette info
		rafraichirLabelIteration();
		// si ce n'est pas la première fois que l'on lance un feu (feu issu d'un clic gauche sur une case)
		if(!firstTime){
 			// on réinitialise la map, voir Case.java pour explications
			pCarte.setMemoireCasesVientDe(new ArrayList<Point>());
			//on retient notre map de cases vient_de, voir Case.java pour explications sur la map
			ArrayList<Point> listeDeCasesVientDe = pCarte.getMemoireCasesVientDe();
			
			// on parcourt toute les cases pour les mettre à jour
			for(int i=0 ; i<pCarte.getHauteur() ; i++){
				for(int j=0 ; j<pCarte.getLargeur() ; j++){
					// on récupère la case
					Case c = pCarte.getGrille()[i][j];
					// si cette case est de l'eau ou un chemin, on passe au tour suivant;
					if(c.getNature() == Nature.EAU || c.getNature() == Nature.CHEMIN){
						continue;
					}
					// sinon, combustion (voir méthode avec un CTRL+clic gauche)
					c.combustion();
					// si la case est en feu, on envoit des brandons
					if(c.getCombustion() == Combustion.EN_FEU || c.getCombustion() == Combustion.BRULEE_CHAUD){
						// application de la matrice de probabilités
						int ligneCentre = pCarte.getVent().trouverCentre().y;
						int colonneCentre = pCarte.getVent().trouverCentre().x;
						int hauteurVent = pCarte.getVent().getMatrice().getHauteur();
						int largeurVent = pCarte.getVent().getMatrice().getLargeur();
						for(int m=0 ; m<hauteurVent ; m++){
							for(int n=0 ; n<largeurVent ; n++){
								int indiceLigne = i + m - ligneCentre;
								int indiceColonne = j + n - colonneCentre;
								if(		indiceColonne>=0 &&
										indiceColonne<pCarte.getLargeur() &&
										indiceLigne>=0 &&
										indiceLigne<pCarte.getHauteur()){
									int pourcentageBrandons = pCarte.getVent().getMatrice().getValeur(m, n);
									int pourcentageHumidite = ((Humidite)cbHumidite.getSelectedItem()).getPourcentage();
									int pourcentageFinal = pourcentageBrandons * pourcentageHumidite / 100;
									if(Math.random()*100 < pourcentageFinal){
										// on récupère la case
										Case ca = pCarte.getGrille()[indiceLigne][indiceColonne];
										// on regarde si elle n'est pas enflammée
										if(ca.getCombustion() == Combustion.RIEN){
											// et si elle est inflammable
											if(ca.getNature() != Nature.EAU && ca.getNature() != Nature.CHEMIN){
												ca.metsLeVientDe();
												// ajout dans la map, voir Case.java pour explications sur la map
												listeDeCasesVientDe.add(new Point(indiceColonne, indiceLigne));
											} // fin si inflammable
										} // fin si non enflammée
									} // fin si bon pourcentage
								} // fin si case n'est pas en dehors de la fenetre
							} // fin parcours matrice proba en ligne n
						} // fin parcours matrice proba en colonne m
					} // if enfeu/brulée chaud
				} // fin for j
			} // fin for i

			// à la fin de ce parcours, si notre map n'est pas vide, voir Case.java pour explications sur la map
			// on la lit
			if(!listeDeCasesVientDe.isEmpty()){
				for(Point p : listeDeCasesVientDe){
					// la Y représente la ligne
		        	int ligne = (int)p.getY();
		        	// la X la colonne
		        	int colonne = (int)p.getX();
		        	pCarte.getGrille()[ligne][colonne].metsLeFeu();
				}
			}

			
			
			// à la fin de chaque itération, on compare la carte d'avant et la carte actuelle
			// pour voir si la simulation est terminée
			if(carteBackup.size() > 1){
				// si elle terminée, on notifie
				// et on stoppe le thread
				if(pCarte.equals(carteBackup.get(carteBackup.size()-1))){
					thVitesse.pause();
					JOptionPane.showMessageDialog(pCarte,"La simulation vient de se terminer.",
							"Simulation terminée", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		
		// on ajoute la carte courante à notre historique
		carteBackup.add(new Carte(pCarte));		
	}
	
	private void actualiserCarte(){
		pGlobal.repaint();
		pGlobal.revalidate();
		carteBackup.clear();
		carteBackup.add(pCarte);
	}
	
	public class ThVitesse extends Thread {
		protected boolean encours;
		private long memoire_temps_ajout_iteration;
		private long memoire_temps_rafraichissement_thread;
		
		public ThVitesse(){
			encours = false;
			memoire_temps_ajout_iteration = System.currentTimeMillis();
			memoire_temps_rafraichissement_thread = System.currentTimeMillis();
		}
		
		public void run() {
			// ce thread marche en continu
			while(true){
				// on appelle le thread toutes les 10 ms
				long actuel = System.currentTimeMillis();
				if(actuel - memoire_temps_rafraichissement_thread >= 10){
				    if(encours){	
				    	long difference = System.currentTimeMillis() - memoire_temps_ajout_iteration;
				    	long tempsEntreDeuxIterations = 1000/vitesse;
				    	if(difference >= tempsEntreDeuxIterations){
				    		ajoutIteration(false);
				    		memoire_temps_ajout_iteration = System.currentTimeMillis();
				    	}
				    }
				    memoire_temps_rafraichissement_thread = System.currentTimeMillis();
				}
			}
		}
		
		public void pause(){
			encours = false;
		}
		
		public void go(){
			encours = true;
		}
	}
	
	/**
	 * --> n'est pas utilisée pour dessiner
	 * est utilisée car est appelée à chaque resize
	 * 
	 * permet de 
	 * 
	 * - replacer les éléments à leur bonne place (extincteur)
	 * 
	 * - redimensionner la case à dessiner
	 */	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		//extin
		pCarte.definirPositionExtincteurs();
		
		//dessin
		Case c = pCarte.getGrille()[0][0];
		pCarte.getLCaseADessiner().setSize(c.getWidth(), c.getHeight());
	}
}


