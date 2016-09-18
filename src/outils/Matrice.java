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
 * FICHIER : Matrice.java
 * 
 * les objets Matrice sont des tableaux à deux dimensions auxquels nous avons ajouté quelques fonctionnalités :
 * 
 * - crop/trim : couper une matrice sur ses brods s'ils contiennent uniquement des 0
 * - rotation d'un angle (non utilisé, gardé d'un version antérieure)
 * - mutliplication de deux matrices (non utilisé, gardé d'un version antérieure)
 * - toString propre
 * 
 */

package outils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


public class Matrice implements Serializable {
	private static final long serialVersionUID = -6717868001338972631L;
	
	
	private final int largeur;
	private final int hauteur;
	

	private int[][] matrice;
	
	/**
	 * On considère la matrice "saine"
	 * sans erreurs, sans null, toutes les colonnes ont le même nb d'éléments, etc
	 */
	public Matrice(int[][] valeurs){
		hauteur = valeurs.length;
		largeur = valeurs[0].length;
		matrice = valeurs;
	}
	
	public String toString(){
		NumberFormat formatter = new DecimalFormat("000");
//		formatter.setMinimumIntegerDigits(7);
//		System.out.println(formatter.format(soldeBancaire));
		
		//resultat: 0056789,12 Euro
		
		String s="";
		for(int i=0; i<hauteur ; i++){
			s+="(";
			for(int j=0 ; j<largeur ; j++){
//				s += Integer.toString(matrice[i][j]);
				s += formatter.format(matrice[i][j]);
				if(j < largeur-1){
					s+=" ";
				}
			}
			s += ")\n";
		}
		return s;
	}
	
	/**
	 * multiplie une matrice de gauche(this) par une matrice de droite(param)
	 * @param m
	 */
	public Matrice multiplier(Matrice m){
		if (hauteur != m.largeur) {
            return null;
        }
		
		int valeurs[][] = new int[hauteur][largeur];
		
		for (int i = 0; i <this. hauteur; i++) { // lignes gauche
            for (int j = 0; j < m.largeur; j++) { // colonnes droite
                for (int k = 0; k < m.largeur; k++) { // colonnes gauche
                	valeurs[i][j] += this.matrice[i][k] * m.matrice[k][j];
                }
            }
        }
		
		Matrice resultat = new Matrice(valeurs);
		
		return resultat;
	}
	
	/**
	 * tourne une matrice de angle radian
	 * @param angle
	 */
	public Matrice rotation(double angle){	
		
		double valeursDouble[][] = {	{Math.cos(angle),	-Math.sin(angle)},
										{Math.sin(angle),	Math.cos(angle)}};
		int valeurs[][] = new int[2][2];
		for(int i=0 ; i<2 ; i++){
			for(int j=0 ; j<2 ; j++){
				valeurs[i][j] = (int)valeursDouble[i][j];
			}
		}
		
		Matrice matriceRotation = new Matrice(valeurs);
		
//		return this.multiplier(matriceRotation);
		return matriceRotation.multiplier(this);
	}
	
	public int getValeur(int i, int j){
		return matrice[i][j];
	}
	
	/**
	 * coupe la matrice :
	 * enlève les lignes et les colonnes qui ne contiennent que des 0
	 * 
	 * exemple :
	 * 
	 * 000
	 * 012
	 * 034
	 * 
	 * devient
	 *  
	 * 12
	 * 34
	 * @return
	 */
	public Matrice crop(){
		// méthode :
		// on va d'abord éliminer toutes les colonnes nulles
		// puis toutes les lignes nulles
		
		// comment faire ?
		
		// pour les colonnes :
		// on parcourt chacune des colonnes une à une
		// si on trouve un entier non nul, on passe à la colonne suivante
		// sinon, on ajoute cette colonne dans un tableau de int appelé "valeurs"
		// (cet ajout se fait en deux temps, d'abord dans une liste [car dynamique], puis dans un tableau [car statique])
		
		// idem pour les lignes avec un tableau appelé valeursFinal
		
		
//		System.out.println("Entrée de Matrice.crop()");
//		System.out.println("Avec la matrice :");
//		System.out.println(this);
		
		// i parcourt les lignes
		// j parcourt les colonnes
		
		ArrayList<int[]> colonnesAGarder = new ArrayList<int[]>();
		// test des colonnes
		// on parcourt chacune des colonnes
		for(int j=0 ; j<largeur ; j++){
			for(int i=0 ; i<hauteur ; i++){
				if(matrice[i][j] != 0){
					int colonne[] = new int [hauteur];
					for(int k=0 ; k<hauteur ; k++){
						colonne[k] = matrice[k][j];
					}
					colonnesAGarder.add(colonne);
					break;
				}
			}
		}
		
		int nombreDeColonnes = colonnesAGarder.size();
		int valeurs[][] = new int[hauteur][nombreDeColonnes];
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<nombreDeColonnes ; j++){
				valeurs[i][j] = colonnesAGarder.get(j)[i];
			}
		}
		
		//
		// idem pour les lignes :
		//
		ArrayList<int[]> lignesAGarder = new ArrayList<int[]>();
		// test des lignes
		// on parcourt chacune des lignes
		for(int i=0 ; i<hauteur ; i++){
			for(int j=0 ; j<nombreDeColonnes ; j++){
				if(valeurs[i][j] != 0){
					lignesAGarder.add(valeurs[i]);
					break;
				}
			}
		}
		
		int nombreDeLignes = lignesAGarder.size();
		int valeursFinal[][] = new int[nombreDeLignes][nombreDeColonnes];
		for(int i=0 ; i<nombreDeLignes ; i++){
			for(int j=0 ; j<nombreDeColonnes ; j++){
				valeursFinal[i][j] = lignesAGarder.get(i)[j];
			}
		}
		
		
//		System.out.println("Sortie de Matrice.crop()");
//		System.out.println("Avec la matrice :");
//		for(int i=0 ; i<nombreDeLignes ; i++){
//			for(int j=0 ; j<nombreDeColonnes ; j++){
//				System.out.print(valeursFinal[i][j]+" ");
//			}
//			System.out.println();
//		}
		
		
		return new Matrice(valeursFinal);
	}
	
	public int getLargeur() {
		return largeur;
	}

	public int getHauteur() {
		return hauteur;
	}
}
