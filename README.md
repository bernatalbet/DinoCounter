# Dino Counter
    
Aplicació Android escrita en Kotlin + Jetpack Compose per l'entorn d'Android Studio, utilitzada per calcular estadístiques pel llibre "7 Dies Sense Llum". Et permet guardar quantitats i l'ordre amb el que apareixen les galetes Dinosaurus, pensada pels packs de 4 galetes per bosseta.

L'idea és prémer el botó "+" per incrementar el comptador parcial, on la suma d'aquests comptadors no pot passar de 4. Quan s'arriba a 4, es deshabiliten els botons "+" i es pot prémer el botó "Guardar":


<table align="center">
    <tr>
        <td><img src="assets/images/PantallaPrincipal.jpg" alt="Pantalla principal" width="250"></td>
        <td></td>
        <td><img src="assets/images/PantallPrincipalAmbDades.jpg" alt="Pantalla principal amb dades" width="250"></td>
    </tr>
</table>

<p>&nbsp</p>

Les dades es guarden en format JSON, on cada galeta té un número assignat del 1 al 6. Per extreure-les es pot prémer el botó "Editar", on es pot seleccionar i copiar el text del JSON resultant. En cas d'equivocació, també es pot editar directament el JSON:

<p align="center">
  <img src="assets/images/JSON.jpg" alt="Pantalla principal" style="width:250px;">
</p>
