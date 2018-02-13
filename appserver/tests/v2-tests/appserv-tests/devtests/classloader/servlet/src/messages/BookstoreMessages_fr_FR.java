/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package messages;

import java.util.*;


public class BookstoreMessages_fr_FR extends ListResourceBundle {
    static final Object[][] contents =
    {
        {
            "ServerError",
            "Votre commande ne peut pas se completer. Le serveur a retourne l'erreur suivante: "
        },
        { "TitleServerError", "Erreur de serveur" },
        { "TitleShoppingCart", "Panier" },
        { "TitleReceipt", "Re" + "\u00e7" + "u" },
        { "TitleBookCatalog", "Catalogue" },
        { "TitleCashier", "Cassier" },
        { "TitleBookDescription", "Description du livre" },
        { "Visitor", "Vous " + "\u00ea" + "tes visiteur numero " },
        { "What", "Ce que nous lisons" },
        {
            "Talk",
            " parle de comment les elements du web peuvent transformer la mani" +
            "\u00e8" + "re dont vous d" + "\u00e9" +
            "veloppez des applications d'internet. Ce livre est obligatoire pour tout d" +
            "\u00e9" + "veloppeur de web!"
        },
        { "Start", "Panier" },
        { "Critics", "Commentaires:  " },
        { "Price", "Prix: " },
        { "CartRemoved", "Vous venez d'enlever " },
        { "CartCleared", "Vous avez vide votre panier" },
        { "CartContents", "Votre panier contient " },
        { "CartItem", " article" },
        { "CartItem", " articles" },
        { "CartAdded1", "Vous avez ajoute " },
        { "CartAdded2", "a votre panier." },
        { "CartCheck", "Verifier votre panier" },
        { "CartAdd", "Ajouter au panier" },
        { "By", "par" },
        { "Buy", "Achetez vos livres" },
        { "Choose", "Choisissez:" },
        { "ItemQuantity", "Quantite" },
        { "ItemTitle", "Titre" },
        { "ItemPrice", "Prix" },
        { "RemoveItem", "Supprimer" },
        { "Subtotal", "Total partiel:" },
        { "ContinueShopping", "Continuer" },
        { "Checkout", "Caisse et Sortie" },
        { "ClearCart", "Vider le panier" },
        { "CartEmpty", "Votre panier est vide." },
        { "Amount", "Le montant de votre achat is:" },
        {
            "Purchase",
            "Pour acheter les articles dans votre panier, veuillez nous donner l'information suivante:"
        },
        { "Name", "Nom:" },
        { "CCNumber", "Numero de votre Carte de Credit:" },
        { "Submit", "Soumettre l'information" },
        { "Catalog", "Retourner au catalogue" },
        { "ThankYou", "Merci de votre achat " },
        { "ThankYouParam", "Merci, {0} pour acheter les livres ici " },
        {
            "OrderError",
            "Votre commande ne peut pas se faire a cause de manque d'inventaire"
        },
        { "With", "Avec" },
        { "Shipping", "Envoi:" },
        { "QuickShip", "Envoi Express" },
        { "NormalShip", "Envoi Normal" },
        { "SaverShip", "Envoi Economique" },
        { "ShipDate", "Votre commande sera envoyee le " },
        { "ShipDateLC", "Votre commande sera envoyee le " },
        { "ConfirmAdd", "Vous avez ajoute \"{0}\" a votre panier" },
        {
            "ConfirmRemove",
            "Vous avez supprim" + "\00e9" + " \"{0}\" de votre panier"
        },
        {
            "CartItemCount",
            "Votre panier contient " +
            "{0,choice,0#aucun article|1#un article|1< {0} articles}"
        },
        { "Newsletters", "Abonnement gratuit a notre bulletin :" },
        { "ThanksMsg", "Merci. Cliquez Soumettre pour acheter les livres." },
        {
            "DukeFanClub",
            "Je veux joindre le Duke Fan Club gratuitement avec mon achat de plus de 100" +
            "\u20a0"
        },
        { "UpdateQuantities", "Mettre a jour" },
        {
            "QuantitiesUpdated",
            "Vous venez de mettre a jour le nombre de livres dans votre panier "
        },
        { "Quantities", "Copies de livres dans le panier" },
        { "ChooseLocale", "Choisissez Votre Language dans la Carte" },
        { "English", "anglais" },
        { "German", "allemand" },
        { "Spanish", "espagnol" },
        { "French", "fran" + "\u00e7" + "ais" },
        { "CustomerInfo", "\u0039" + "crivez votre information dans la forme." },
        {
            "BookCatalog",
            "Ajoutez les livres du catalogue" + "\u00c1" + "votre caddie."
        },
        {
            "ShoppingCart",
            "Cette page " + "\u00e9" + "num" + "\u00e8" +
            "re les livres dans votre caddie."
        }
    };

    public Object[][] getContents() {
        return contents;
    }
}
