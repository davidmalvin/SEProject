package com.company;
import java.sql.*;
import java.util.*;
public class RechercheFilm {
    Connection connection;
    Statement statement;
    InfoFilm infoFilm;

    public RechercheFilm(String nomFicherSQLite) {


        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + nomFicherSQLite);
            statement = connection.createStatement();
            System.out.println("Connection to " + nomFicherSQLite + " etablished");
            Scanner SC = new Scanner(System.in);
            System.out.println("Enter your request:");
            String Request = SC.nextLine();
            String Result = RechercheFilm.GoodRequest(Request);
            String JsonResult = Retrouve(Result);
            System.out.println(JsonResult);
            FermeBase();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error: no connection ! ");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Error: no connection !");
        }


    }


    public void FermeBase() {
        try {
            connection.close();
            statement.close();
            System.out.println("The database is closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    /***
     * Première idée de requête SQL
     * @param Request
     * @return
     * @throws ClassNotFoundException
     */
    public String Retrouve(String Request) throws ClassNotFoundException {
        String Json = "";
        String nomActeurs;
        String prenomActeurs;
        String nomRealisateur;
        String prenomRealisateur;
        String titre;
        NomPersonne realisateur;
        NomPersonne acteur;
        ArrayList<NomPersonne> realisateurs = new ArrayList<>();
        ArrayList<NomPersonne> acteurs = new ArrayList<>();
        String pays;
        Integer annee;
        Integer duree;
        ArrayList<String> autres_titres = new ArrayList<>();

        String[] splitrequete = Request.split(" ");
        StringBuilder data = new StringBuilder();

        for (int i = 1; i < splitrequete.length; i++) {
            data = data.append(splitrequete[i]);
            data = data.append(" ");
        }
        switch (splitrequete[0]) {
            case "TITRE":
                if (connection != null) try {

                    PreparedStatement filter = connection.prepareStatement("WITH filtre AS (SELECT id_film FROM (SELECT id_film FROM films WHERE films.titre = ?" +
                            "UNION " +
                            "SELECT id_film FROM autres_titres WHERE autres_titres.titre LIKE ?))" +
                            "SELECT films.*, GROUP_CONCAT(autres_titres.titre, '|') AS autres_titres, personnes.prenom, personnes.nom, generique.role FROM filtre " +
                            "                                    JOIN films ON films.id_film = filtre.id_film " +
                            "                                    JOIN pays ON pays.code = films.pays" +
                            "                                    LEFT JOIN autres_titres ON autres_titres.id_film = films.id_film " +
                            "                                    JOIN generique ON generique.id_film = films.id_film " +
                            "                                    JOIN personnes ON personnes.id_personne = generique.id_personne " +
                            "                                    GROUP BY personnes.prenom, personnes.nom, generique.role");
                    filter.setString(1, "Godzilla");
                    filter.setString(2, "Godzilla");


                    ResultSet rs = filter.executeQuery();
                    ResultSetMetaData resultMeta = rs.getMetaData();

                    PreparedStatement resActeur = connection.prepareStatement("SELECT prenom, nom FROM (WITH filtre AS (SELECT id_film FROM (SELECT id_film FROM films WHERE films.titre = ?" +
                            "                                    UNION " +
                            "                                    SELECT id_film FROM autres_titres WHERE autres_titres.titre LIKE ?))" +
                            "                                    SELECT films.*, GROUP_CONCAT(autres_titres.titre, '|') AS autres_titres, personnes.prenom, personnes.nom, generique.role FROM filtre " +
                            "                                                                        JOIN films ON films.id_film = filtre.id_film" +
                            "                                                                        JOIN pays ON pays.code = films.pays" +
                            "                                                                        LEFT JOIN autres_titres ON autres_titres.id_film = films.id_film " +
                            "                                                                        JOIN generique ON generique.id_film = films.id_film " +
                            "                                                                        JOIN personnes ON personnes.id_personne = generique.id_personne " +
                            "                                                                        GROUP BY personnes.prenom, personnes.nom, generique.role) WHERE role = \"A\"");
                    resActeur.setString(1, "Godzilla");
                    resActeur.setString(2, "Godzilla");
                    ResultSet rs2 = resActeur.executeQuery();

                    PreparedStatement resRealisateur = connection.prepareStatement("SELECT prenom, nom FROM (WITH filtre AS (SELECT id_film FROM (SELECT id_film FROM films WHERE films.titre = ?" +
                            "                                    UNION " +
                            "                                    SELECT id_film FROM autres_titres WHERE autres_titres.titre LIKE ?))" +
                            "                                    SELECT films.*, GROUP_CONCAT(autres_titres.titre, '|') AS autres_titres, personnes.prenom, personnes.nom, generique.role FROM filtre " +
                            "                                                                        JOIN films ON films.id_film = filtre.id_film" +
                            "                                                                        JOIN pays ON pays.code = films.pays" +
                            "                                                                        LEFT JOIN autres_titres ON autres_titres.id_film = films.id_film " +
                            "                                                                        JOIN generique ON generique.id_film = films.id_film " +
                            "                                                                        JOIN personnes ON personnes.id_personne = generique.id_personne " +
                            "                                                                        GROUP BY personnes.prenom, personnes.nom, generique.role) WHERE role = \"R\"");
                    resRealisateur.setString(1, "Godzilla");
                    resRealisateur.setString(2, "Godzilla");
                    ResultSet rs3 = resRealisateur.executeQuery();
                    while (rs.next()) {

                        //Autres titres
                        if (rs.getString("autres_titres") == null) {
                            autres_titres.add("");
                        } else {
                            autres_titres.add(rs.getString("autres_titres"));
                        }
                        //Réalisateurs
                        while (rs3.next()) {
                            if (realisateurs == null) {
                                break;
                            } else {

                                nomRealisateur = rs3.getString("nom");
                                prenomRealisateur = rs3.getString("prenom");
                                realisateur = new NomPersonne(nomRealisateur, prenomRealisateur);
                                realisateurs.add(realisateur);
                            }
                        }
                        //Acteurs
                        while (rs2.next()) {
                            if (acteurs == null) {
                                break;
                            } else {
                                nomActeurs = rs2.getString("nom");
                                prenomActeurs = rs2.getString("prenom");
                                acteur = new NomPersonne(nomActeurs, prenomActeurs);
                                acteurs.add(acteur);
                            }
                        }
                        titre = rs.getString("titre");
                        pays = rs.getString("pays");
                        annee = rs.getInt("annee");
                        duree = rs.getInt("duree");


                        System.out.println("Titre : " + titre);
                        for (int i = 0; i < realisateurs.size(); i++) {
                            System.out.println("Realisateurs : " + realisateurs.get(i));
                        }
                        for (int i = 0; i < acteurs.size(); i++) {
                            System.out.println("Acteurs : " + acteurs.get(i));
                        }
                        System.out.println("Pays : " + pays);
                        System.out.println("Année : " + annee);
                        System.out.println("Durée : " + duree);
                        for (int i = 0; i < autres_titres.size(); i++) {
                            System.out.println("Autres titres : " + i + autres_titres.get(i));
                        }

                        InfoFilm infoFilm = new InfoFilm(titre, realisateurs, acteurs, pays, annee, duree, autres_titres);
                        Json = Json + infoFilm.toString() + "\n";
                    }

                    rs.close();
                    rs2.close();
                    rs3.close();

                   // System.out.println(infoFilm.toString());

                            /*else{
                        System.out.println("NO RESULT !!!!!!!!");
                    }*/
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "DE":

                break;
            case "AVEC":
                break;
            case "PAYS":
                try {
                    ArrayList<String> listFilms = new ArrayList<String>();
                    StringBuilder SQLrequest = new StringBuilder();
                    SQLrequest.append("SELECT films.titre FROM films WHERE films.pays = \"");
                    SQLrequest.append("gb" + "\"");
                    SQLrequest.append(".");
                    PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(SQLrequest));

                    preparedStatement.setString(1, String.valueOf(data));

                    ResultSet rsPays = preparedStatement.executeQuery();
                    ResultSetMetaData resultSetMetaData = rsPays.getMetaData();

                    while (rsPays.next()) {
                       // System.out.println("Rs2 :" +rs2);
                       // listFilms.add(String.valueOf(rs2));


                        for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                            listFilms.add(rsPays.getString("titre"));
                        }
                    }

                    System.out.println("En " + data + "titre : ");
                    for (int i = 0; i < listFilms.size(); i++) {
                        System.out.println("\t" + listFilms.get(i));
                    }

                    rsPays.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "EN":
                //Faire condition, si donnée est un entier ou pas
                PreparedStatement psAnnee = null;
                try {
                    StringBuilder SQLrequest = new StringBuilder();
                    SQLrequest.append("SELECT films.* FROM films WHERE films.annee = ");
                    SQLrequest.append(data);
                        /*psAnnee = dataBase.prepareStatement("SELECT films.* FROM films WHERE films.annee = ?");
                        psAnnee.setInt(1, 1988);*/
                    psAnnee = connection.prepareStatement(String.valueOf(SQLrequest));
                    ResultSet rsEn = psAnnee.executeQuery();

                    while (rsEn.next()) {
                        titre = rsEn.getString("titre");
                        pays = rsEn.getString("pays");
                        annee = rsEn.getInt("annee");
                        duree = rsEn.getInt("duree");

                        InfoFilm infoFilm = new InfoFilm(titre,realisateurs,acteurs,pays,annee,duree,autres_titres);
                         Json = Json + infoFilm.toString() + "\n";
                        System.out.println(infoFilm.toString());
                    }

                    rsEn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;
            case "AVANT":
                try {
                    StringBuilder SQLrequest = new StringBuilder();
                    SQLrequest.append("SELECT films.* FROM films WHERE films.annee < ");
                    SQLrequest.append(data);
                    SQLrequest.append("GROUP BY films.annee");
                    PreparedStatement psAvant = connection.prepareStatement(String.valueOf(SQLrequest));
                    //psAvant.setInt(1, 1988);
                    ResultSet rsAvant = psAvant.executeQuery();

                    while (rsAvant.next()) {
                        titre = rsAvant.getString("titre");
                        pays = rsAvant.getString("pays");
                        annee = rsAvant.getInt("annee");
                        duree = rsAvant.getInt("duree");

                        InfoFilm infoFilm = new InfoFilm(titre,realisateurs,acteurs,pays,annee,duree,autres_titres);
                        Json = Json + infoFilm.toString() + "\n";
                        System.out.println(infoFilm.toString());
                    }
                    rsAvant.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "APRES":
                try {
                    StringBuilder SQLrequest = new StringBuilder();
                    SQLrequest.append("SELECT films.* FROM films WHERE films.annee >= ");
                    SQLrequest.append(data);
                    SQLrequest.append("GROUP BY films.annee");
                    PreparedStatement psApres = connection.prepareStatement(String.valueOf(SQLrequest));
                    psApres.setInt(1, 1988);
                    ResultSet rsApres = psApres.executeQuery();

                    while (rsApres.next()) {
                        titre = rsApres.getString("titre");
                        pays = rsApres.getString("pays");
                        annee = rsApres.getInt("annee");
                        duree = rsApres.getInt("duree");

                        InfoFilm infos = new InfoFilm(titre,realisateurs,acteurs,pays,annee,duree,autres_titres);
                        Json = Json + infos.toString() + "\n";
                        System.out.println(infos.toString());
                    }
                    rsApres.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
        return Json;
    }

    private static String GoodRequest(String Request) {
        String[] arraywords = Request.split(" ");

        //Certains mots sont incomplets c'est au cas où si l'utilisateur fait une éventuelle faute d'orthographe
        if (arraywords[0].toLowerCase().startsWith("titre")) {
            System.out.println("TITRE !!");
            arraywords[0] = arraywords[0].toUpperCase();
            arraywords[0] = arraywords[0].replace(arraywords[0], "TITRE");

        } else if (arraywords[0].toLowerCase().startsWith("de")) {
            System.out.println("DE !!");
            arraywords[0] = arraywords[0].replace(arraywords[0], "DE");

        } else if (arraywords[0].toLowerCase().startsWith("ave")) {
            System.out.println("AVEC !!");
            arraywords[0] = arraywords[0].replace(arraywords[0], "AVEC");

        } else if (arraywords[0].toLowerCase().startsWith("pay")) {
            System.out.println("PAYS !!");
            arraywords[0] = arraywords[0].replace(arraywords[0], "PAYS");

        } else if (arraywords[0].toLowerCase().startsWith("en")) {
            System.out.println("EN !!");
            arraywords[0] = arraywords[0].replace(arraywords[0], "EN");

        } else if (arraywords[0].toLowerCase().startsWith("ava")) {
            System.out.println("AVANT !!");
            arraywords[0] = arraywords[0].replace(arraywords[0], "AVANT");

        } else if (arraywords[0].toLowerCase().startsWith("apre")) {
            System.out.println("APRES !!!");
            arraywords[0] = arraywords[0].replace(arraywords[0], "APRES");

        }


        StringBuilder text = new StringBuilder();
        for (int i = 0; i < arraywords.length; i++) {
            text.append(arraywords[i]);
            text.append(" ");
        }

        return String.valueOf(text);


    }
}
