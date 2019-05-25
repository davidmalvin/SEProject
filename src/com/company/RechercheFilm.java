import java.sql.*;
import java.util.*;
/**
 * Open the database and close the datebase at the same time while entering the request that you ask for.
 * @return the good SQL reply in Java
 */

public class RechercheFilm {
    Connection connection;
    Statement statement;
    private HashMap<String,String> myRequest = new HashMap<>();


    public RechercheFilm(String nomFicherSQLite) {





        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + nomFicherSQLite);
            statement = connection.createStatement();
            System.out.println("Connection to " + nomFicherSQLite + " etablished ");


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error: no connection ! ");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Error: no connection !");
        }


    }






    /**
     *
     * Creation of the constructor RechercheFilm.
     * Initialize the database.
     *
     * @param requete
     * @return JSON object which contains the SQL request.
     */


    public String retrouve(String requete) {
        


        myRequest.put("TITRE", "SELECT id_film FROM recherche_titre WHERE titre match \"%s\"");
        myRequest.put("DE", "SELECT id_film FROM generique join personnes ON generique.id_personne = personnes.id_personne WHERE ((personnes.prenom like '%%'||\"%s\"||'%%' AND personnes.nom like '%%'||\"%s\"||'%%') OR (personnes.prenom like '%%'||\"%s\"||'%%' AND personnes.nom like '%%'||\"%s\"||'%%')) AND generique.role like 'R'");
        myRequest.put("AVEC", "SELECT id_film FROM generique join personnes ON generique.id_personne = personnes.id_personne WHERE ((personnes.prenom like  \"%s\" AND personnes.nom like '%%'||\"%s\"||'%%') OR (personnes.prenom like '%%'||\"%s\"||'%%' AND personnes.nom like '%%'||\"%s\"||'%%')) AND generique.role like 'A'");
        myRequest.put("PAYS", "SELECT id_film FROM films join pays ON films.pays = pays.code WHERE pays.nom like '%%'||\"%s\"||'%%' OR pays.code like \"%s\"");
        myRequest.put("EN", "SELECT id_film FROM films WHERE films.annee = %s");
        myRequest.put("AVANT", "SELECT id_film FROM films WHERE films.annee < %s");
        myRequest.put("APRES", "SELECT id_film FROM films WHERE films.annee > %s");
        StringBuilder sb = new StringBuilder("WITH filtre AS ( ");
        for (String s : requestToSQL(requete)) {
            sb.append(s);
            if (s.contains("\"error\"")) return s;
        }
        sb.append(" ) SELECT f.id_film, f.titre, Group_concat(a.titre, '|') AS autres_titres, p.prenom, p.nom, g.role, f.annee, py.nom as pays, f.duree FROM   filtre JOIN films f ON f.id_film = filtre.id_film JOIN pays py   ON py.code = f.pays LEFT JOIN autres_titres a        ON a.id_film = f.id_film JOIN generique g ON g.id_film = f.id_film JOIN personnes p ON p.id_personne = g.id_personne GROUP BY f.id_film, f.titre, p.prenom, p.nom, g.role ORDER BY f.annee DESC, f.titre");
        try {
            PreparedStatement stmt;
            stmt = connection.prepareStatement(sb.toString());
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                return "{\"result\":[]}";
            }
            sb = new StringBuilder();
            List<String> alTitres = new ArrayList<>();
            ArrayList<NomPersonne> listeRealisateurs = new ArrayList<>();
            ArrayList<NomPersonne> listeActeur = new ArrayList<>();

            int duree = 0;
            String pays = new String("");
            int annee = 0;

            String titre;
            String autres_titres;
            String titrePrecedent = new String("");
            boolean premiereBoucle = true;
            int ite = 0;
            do {
                titre = results.getString("titre");
                if (!titrePrecedent.equals(titre) && !premiereBoucle) {
                    sb.append(new InfoFilm(titrePrecedent, listeRealisateurs, listeActeur, pays, annee, duree, new ArrayList<>(alTitres)).toString()).append(", ");
                    listeActeur = new ArrayList<>();
                    listeRealisateurs = new ArrayList<>();
                }
                if (!titrePrecedent.equals(results.getString("titre"))) {
                    ite++;
                    autres_titres = results.getString("autres_titres");
                    duree = results.getInt("duree");
                    pays = results.getString("pays");
                    annee = results.getInt("annee");
                    if (autres_titres != null)
                        alTitres = Arrays.asList(autres_titres.split("\\|"));
                    else
                        alTitres = new ArrayList<>();
                }
                if (results.getString("role").equals("R")) {
                    listeRealisateurs.add(new NomPersonne(results.getString("nom"), results.getString("prenom")));
                } else if (results.getString("role").equals("A")) {
                    listeActeur.add(new NomPersonne(results.getString("nom"), results.getString("prenom")));
                }
                titrePrecedent = titre;
                premiereBoucle = false;

            } while (results.next() && ite < 100);
            sb.append(new InfoFilm(titrePrecedent, listeRealisateurs, listeActeur, pays, annee, duree, new ArrayList<String>(alTitres)).toString());
            if (ite >= 100) {
                sb.append(", {\"info\":\"Search results are limited to 100 \"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "{\"result\":[" + sb.toString() + "]}";
    }


    public ArrayList<String> requestToSQL(String request) {

        request = request.replace(", ", " , ");
        String[] tab = request.split(" ");

        ArrayList<String> arrayList = new ArrayList<>();
        String current;
        for (int k = 0; k < tab.length; k++) {
            ;

            if (myRequest.containsKey(tab[k].toUpperCase())) {
                current = tab[k];
                ArrayList<String> temp = new ArrayList<>();
                k++;
                while (k < tab.length && !myRequest.containsKey(tab[k].toUpperCase())) {
                    if (k < tab.length - 1 && (myRequest.containsKey(tab[k+1].toUpperCase()))) {
                        break;
                    }
                    else if (k+1 >= tab.length) {
                        temp.add(tab[k]);
                        arrayList.add(String.format((String) myRequest.get(current), (Object[]) verifTitre(temp, current).toArray()));
                    } else if ((tab[k+1].equals(",") || tab[k+1].toUpperCase().equals("OU"))) {
                        temp.add(tab[k]);
                        arrayList.add(String.format((String) myRequest.get(current), (Object[]) verifTitre(temp, current).toArray()));
                        if ((tab[k+1].equals(","))) {
                            arrayList.add(" INTERSECT ");
                        } else if (tab[k+1].toUpperCase().equals("OU")) {
                            arrayList.add(" UNION ");
                        }
                        temp.clear();
                    } else if(!(tab[k].equals(",") || tab[k].toUpperCase().equals("OU"))){
                        temp.add(tab[k]);
                    }
                    k++;
                }
            } else {
                ArrayList<String > error = new ArrayList<>();
                error.add("{\"error\": \"Error while entering your request, please try again.\"}");
                return error;
            }
        }

        return arrayList;
    }

    /** -> Analyze and build the SQL request in order to find the good answer.
     * @param
     * @return  arrayList
     */

    public void FermeBase() {
        try {
            connection.close();
            statement.close();
            System.out.println("The database is closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }



    private ArrayList<String> verifTitre(ArrayList<String> al, String s) {

        if (s.equals("TITRE")) {
            StringBuilder sb = new StringBuilder();
            for (String st : al) {
                sb.append(st).append(" ");
            }
            ArrayList<String> ret = new ArrayList<>();
            ret.add(sb.substring(0, sb.length() - 1));
            return ret;
        }
         else if (s.equals("PAYS")) {
            al.add(al.get(0));
        }
        else if (s.equals("AVEC") || s.equals("DE")) {
            al.add(al.get(1));
            al.add(al.get(0));
        }
        return al;
    }
}
/**
 * @param al -> keywords of what you are looking for and refine search results.
 * @return  ArrayList
 * You have to write proprely and correctly the file.
 */



