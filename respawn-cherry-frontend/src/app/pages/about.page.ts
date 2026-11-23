import { Component } from '@angular/core';

@Component({
  selector: 'app-about-page',
  template: `
    <div class="page-heading">
      <div>
        <span class="eyebrow">NOSOTR@S</span>
        <h1 class="category-title">Conoce mas de esta comunidad <span class="title-heart">♡</span></h1>
      </div>
    </div>
    <article class="card about-card">
      <h2>Nosotr@s 🍒</h2>
      <p class="about-lead">Respawn Cherry: un espacio donde las gamers tienen su lugar.</p>
      <p>Respawn Cherry es una comunidad creada por mujeres y para mujeres, pensada para conectar, compartir y dar visibilidad a quienes forman parte del mundo de los videojuegos.</p>
      <p>Nace con la idea de construir un espacio seguro, inclusivo y libre de prejuicios, donde cada gamer pueda expresarse, conocer nuevas personas, encontrar compañeras de juego y compartir su pasión sin importar su experiencia o nivel.</p>
      <p>Nuestra plataforma permite crear publicaciones, compartir consejos, mostrar setups, descubrir nuevos videojuegos, buscar compañeras para formar squads y conversar con otras mujeres que comparten los mismos intereses.</p>
      <p>Creemos que jugar también es conectar, aprender, crear amistades y sentirse parte de algo. Por eso queremos fomentar una comunidad donde todas tengan voz, puedan apoyarse mutuamente y disfrutar del gaming con libertad y confianza.</p>
      <p class="about-close">Porque el mundo gamer también es nuestro. Y aquí, todas tenemos un lugar. 🍒</p>
    </article>
  `,
  styles: [`
    .page-heading h1.category-title { line-height: 1.05; }
    .title-heart { font-family: Manrope, sans-serif; }
    .about-card { padding: 28px 30px 26px; display: grid; gap: 14px; }
    .about-card h2 { margin: 0; font: 800 22px Manrope, sans-serif; letter-spacing: -.3px; }
    .about-card p.about-lead { margin: 0; color: #ffb3d0; font-size: 16px; font-style: italic; font-weight: 600; line-height: 1.45; }
    .about-card p { margin: 0; color: #f5eef2; font-size: 14px; line-height: 1.65; }
    .about-close { font-weight: 800; }
    @media (max-width: 720px) {
      .about-card { padding: 20px 16px; }
      .about-card h2 { font-size: 18px; }
      .about-lead, .about-card p { font-size: 13px; }
    }
  `]
})
export class AboutPage {}
