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
 * FICHIER : Main.java
 * 
 * Point de départ du programme car contient le main
 * Lance uniquement une Fenêtre
 */

package tout;


public class Main {	
	public static void main(String[] args){
		//Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new Fenetre();
            }
        });
	}
}
