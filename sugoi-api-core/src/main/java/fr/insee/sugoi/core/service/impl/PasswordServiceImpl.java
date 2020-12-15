/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.core.service.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.sugoi.core.service.PasswordService;

@Service
public class PasswordServiceImpl implements PasswordService {

  private static final int LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE = 8;
  private static final int NOMBRE_CARACTERE_NON_SPECIAUX_POUR_INITIALISATION = 2;
  // les miniscules sauf le i, le l et le o
  private static final String MINUSCULES = "abcdefghjkmnpqrstuvwxyz";
  private static final Character[] MINUSCULES_ARRAY = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p',
      'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
  // les majuscules sauf le I, le O et le Q
  private static final String MAJUSCULES = "ABCDEFGHJKLMNPRSTUVWXYZ";
  private static final Character[] MAJUSCULES_ARRAY = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N',
      'P', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
  // les chiffres sauf le 1 et le 0
  private static final String CHIFFRES = "23456789";
  private static final Character[] CHIFFRES_ARRAY = { '2', '3', '4', '5', '6', '7', '8', '9' };
  // point d'exclamation, dollar, pourcent, esperluette, parentheses, asterisque,
  // plus, point
  // d'interrogation et arobase
  private static final String SPECIAUX = "!$%&()*+?@";
  private static final Character[] SPECIAUX_ARRAY = { '!', '$', '%', '&', '(', ')', '*', '+', '?', '@' };
  private static final List<Character> SPECIAUX_LIST = Arrays.asList(SPECIAUX_ARRAY);
  private static final int NB_ITERATION_MAX = 20;

  private static final Logger LOG = LogManager.getFormatterLogger(PasswordServiceImpl.class);

  /**
   * Génère un certain nombre de mot passe selon la taille souhaitée.
   *
   * @param passwordLength la taille des mot de passe à générer
   * @param nbPassword     le nombre à générer
   * @return la liste des mots de passe demandée.
   */
  public List<String> generatePasswords(Integer passwordLength, Integer nbPassword) {
    List<String> passwords = new ArrayList<String>();
    for (int i = 0; i < nbPassword; i++) {
      passwords.add(generatePassword(passwordLength));
    }
    return passwords;
  }

  /**
   * Génère un mot de passe de la taille souhaitée.
   *
   * @param longueur taille souhaitée
   * @return le mot de passe
   */
  public String generatePassword(int longueur) {
    if (longueur < LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE) {
      longueur = LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE;
    }

    // Nombre de caractères du password
    int passwordLength = longueur;
    SecureRandom random = new SecureRandom();

    // Nombres de chiffres;
    // tirage dans [|0,lenght-1|] donc leght-2 exclu puis +1 pour en avoir 1 au
    // moins
    int nbDigits = random.nextInt(passwordLength - 2) + 1;

    // Nombres de caractères spéciaux fixé à 1
    int nbSpecials = 1;

    // le reste en lettres
    int nblettres = passwordLength - nbDigits - nbSpecials;

    List<Character> caracteresDuPassword = new ArrayList<>();

    // tirage des lettres
    for (int i = 0; i < nblettres; i++) {
      // tirage au sort majuscule ou minuscule
      if (random.nextBoolean()) {
        caracteresDuPassword.add(MAJUSCULES_ARRAY[random.nextInt(MAJUSCULES_ARRAY.length)]);
      } else {
        caracteresDuPassword.add(MINUSCULES_ARRAY[random.nextInt(MINUSCULES_ARRAY.length)]);
      }
    }

    // tirage des chiffres
    for (int i = 0; i < nbDigits; i++) {
      caracteresDuPassword.add(CHIFFRES_ARRAY[random.nextInt(CHIFFRES_ARRAY.length)]);
    }

    // tirage des caracteres speciaux
    for (int i = 0; i < nbSpecials; i++) {
      caracteresDuPassword.add(SPECIAUX_ARRAY[random.nextInt(SPECIAUX_ARRAY.length)]);
    }

    Collections.shuffle(caracteresDuPassword);
    int nbEssai = 0;
    // on ne veut pas de charactere speciaux en premier et dernier charactere
    // on essaye jusqu'à que ce soit le cas et au pire on abandonne apres
    // NB_ITERATION_MAX
    // iterations
    // pour taille=8 et nbmax=20, cela se produit avec 1 chance sur
    // 1.099.511.627.776
    while ((SPECIAUX_LIST.contains(caracteresDuPassword.get(0))
        || SPECIAUX_LIST.contains(caracteresDuPassword.get(caracteresDuPassword.size() - 1)))
        && nbEssai < NB_ITERATION_MAX) {
      Collections.shuffle(caracteresDuPassword);
      nbEssai++;
    }

    StringBuffer sb = new StringBuffer();
    caracteresDuPassword.forEach(c -> sb.append(c));

    return sb.toString();
  }

  /**
   * Vérifie que le mot de passe passé en paramètre respecte bien la politique de
   * mot de passe à usage unique de l'Insee. Au minimum à 8, la longueur minimale
   * à tester peut être choisie.
   *
   * @param motDePasse       à tester
   * @param longueurMinimale du test
   * @return vrai si la taille est bonne et la politique respectée
   */
  public boolean verifierFormatInitMotDePasse(String motDePasse, int longueurMinimale) {
    if (longueurMinimale < LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE) {
      longueurMinimale = LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE;
    }
    if ((motDePasse == null) || (motDePasse.length() < longueurMinimale)) {
      LOG.debug("longueur du mdp inférieure à la longueur minimale ou mot de passe null");
      return false;
    }
    if (verifierExpReg(motDePasse,
        "[" + MINUSCULES + MAJUSCULES + CHIFFRES + "]([" + MINUSCULES + MAJUSCULES + CHIFFRES + SPECIAUX + "]{"
            + (motDePasse.length() - NOMBRE_CARACTERE_NON_SPECIAUX_POUR_INITIALISATION) + ",})[" + MINUSCULES
            + MAJUSCULES + CHIFFRES + "]")
        && (countMatches(motDePasse, "[" + SPECIAUX + "]") == 1)) {
      int compteur = 0;

      compteur += verifierExpReg(motDePasse, "^(.*[" + MINUSCULES + "].*)$") ? 1 : 0;
      compteur += verifierExpReg(motDePasse, "^(.*[" + MAJUSCULES + "].*)$") ? 1 : 0;
      compteur += verifierExpReg(motDePasse, "^(.*[" + CHIFFRES + "].*)$") ? 1 : 0;
      compteur += verifierExpReg(motDePasse, "^(.*[" + SPECIAUX + "].*)$") ? 1 : 0;
      return (compteur == 3 || compteur == 4);
    }
    return false;
  }

  /**
   * Vérifie que le mot de passe passé en paramètre respecte bien la politique de
   * mot de passe de l'Insee lors d'un changement de mot de passe. Au minimum à 8,
   * la longueur minimale à tester peut être choisie
   *
   * @param motDePasse       à tester
   * @param longueurMinimale à vérifier
   * @return vrai si la longueur est suffisant et que la politique est respectée
   */
  public boolean verifierFormatChangeMotDePasse(String motDePasse, int longueurMinimale) {
    if (longueurMinimale < LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE) {
      longueurMinimale = LONGUEUR_MINIMALE_PASSWORD_POLITIQUE_INSEE;
    }
    if ((motDePasse == null) || (motDePasse.length() < longueurMinimale)) {
      LOG.debug("longueur du mdp inférieure à la longueur minimale ou mot de passe null");
      return false;
    }
    int compteur = 0;
    compteur += verifierExpReg(motDePasse, "^(.*[a-z].*)$") ? 1 : 0;
    compteur += verifierExpReg(motDePasse, "^(.*[A-Z].*)$") ? 1 : 0;
    compteur += verifierExpReg(motDePasse, "^(.*[0-9].*)$") ? 1 : 0;
    compteur += verifierExpReg(motDePasse, "^(.*[^a-zA-Z0-9].*)$") ? 1 : 0;
    return (compteur == 3 || compteur == 4);
  }

  /**
   * Retourne true si le paramètre est non null et s'il est compatible avec
   * l'expression régulière fournie.
   *
   * @param valeurTest : valeur à controler
   * @param expReg     : expression régulière pour le contrôle
   * @return true si v respecte l'expression régulière expReg
   */
  public static boolean verifierExpReg(String valeurTest, String expReg) {
    if (valeurTest != null) {
      Pattern pattern;
      pattern = Pattern.compile(expReg);
      Matcher matcher = pattern.matcher(valeurTest);
      return matcher.matches();
    } else {
      return false;
    }
  }

  /**
   * Retourne le nombre de fois où l'expression régulière expReg est retrouvée
   * dans la chaîne v.<br>
   * Retourne 0 si v est null.
   *
   * @param valeurTest : valeur à contrôler
   * @param expReg     : expression régulière pour le contrôle
   * @return nombre de fois où expReg est retrouvée dans v.
   */
  public static int countMatches(String valeurTest, String expReg) {
    int count = 0;
    if (valeurTest != null) {
      Pattern pattern;
      pattern = Pattern.compile(expReg);
      Matcher matcher = pattern.matcher(valeurTest);
      while (matcher.find()) {
        count++;
      }
    }
    return count;
  }
}
