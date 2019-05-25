package com.company;

public class Main {

    public static void main(String[] args) {
        RechercheFilm rechercheFilm = new RechercheFilm("/Users/davidobiangnzue/Desktop/bdfilm.sqlite");
        System.out.println(rechercheFilm.retrouve("TITRE James Bond "));
        rechercheFilm.FermeBase();

    }
}
