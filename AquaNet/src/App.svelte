<script lang="ts">
  import { Route, Router } from "svelte5-router";
  import Welcome from "./pages/Welcome.svelte";
  import UserHome from "./pages/UserHome.svelte";
  import Home from "./pages/Home.svelte";
  import Ranking from "./pages/Ranking.svelte";
  import { CARD, USER } from "./libs/sdk";
  import type { AquaNetUser } from "./libs/generalTypes";
  import Settings from "./pages/User/Settings.svelte";
  import { pfp, tooltip } from "./libs/ui"
  import { ANNOUNCEMENT } from "./libs/config";
  import { t } from "./libs/i18n";
  import logo from "./assets/imgs/munet.avif";

  console.log(`%c
┏━┓         ┳━┓━┓┏━
┣━┫┏━┓┓ ┏┏━┓┃ ┃ ┣┫
┛ ┗┗━┫┗━┻┗━┻┻━┛━┛┗━
     ┗       v${APP_VERSION}`, `
     background: linear-gradient(-45deg, rgba(18,194,233,1) 0%, rgba(196,113,237,1) 50%, rgba(246,79,89,1) 100%);
     font-size: 2em;
     font-family: Monospace;
     unicode-bidi: isolate;
     -webkit-background-clip: text;
     -webkit-text-fill-color: transparent;`)

  export let url = "";
  let me: AquaNetUser

  if (USER.isLoggedIn())
  {
    USER.me().then(m => {
      me = m
    }).catch(e => console.error(e))

    const themeStyle = document.createElement("link");
    themeStyle.rel = "stylesheet";
    switch (localStorage.getItem("theme")) {
      case "cn":
        themeStyle.href = "/assets/theme/cn.css";
    };
    if (themeStyle.href)
      document.head.appendChild(themeStyle);
  }
  let path = window.location.pathname;
</script>

<nav>
  {#if path !== "/legacy" && path !== "/legacy/"}
    <a class="logo" href="/">
      <img src={logo} alt="">
    </a>
  {/if}
  {#if ANNOUNCEMENT}
    <div class="announcement">
      <strong>{t('navigation.notice')}</strong>: {ANNOUNCEMENT}
    </div>
  {/if}
  <a href="/legacy/home">{t('navigation.home').toLowerCase()}</a>
  <a href="/legacy/ranking">{t('navigation.rankings').toLowerCase()}</a>
  {#if me}
    <a href="/legacy/u/{me.username}" use:tooltip={t('navigation.profile')}>
      <img alt="profile" class="pfp" use:pfp={me}/>
    </a>
  {/if}
</nav>

<Router {url}>
  <Route path="/legacy" component={Welcome} />
  <Route path="/legacy/" component={Welcome} />
  <Route path="/legacy/home" component={Home} />
  <Route path="/legacy/ranking" component={Ranking} />
  <Route path="/legacy/ranking/:game" component={Ranking} />
  <Route path="/legacy/u/:username" component={UserHome} />
  <Route path="/legacy/u/:username/:game" component={UserHome} />
  <Route path="/legacy/settings" component={Settings} />
</Router>

<style lang="sass">
  @use "vars"

  nav
    display: flex
    justify-content: flex-end
    align-items: center
    gap: 32px
    height: vars.$nav-height

    padding: 0 48px

    z-index: 10
    position: relative

    img
      height: 2rem
      object-fit: cover

    .announcement
      position: absolute
      left: 50%
      transform: translate(-50%, 0)
      top: 0
      width: 50%
      height: 100%
      display: flex
      justify-content: center
      align-content: center
      z-index: -1
      background: linear-gradient(90deg, #6f0f0f00 0%, vars.$c-shadow 50%, #6f0f0f00 100%)
      font-size: 1.125em
      text-decoration: none !important
      color: inherit !important

    .pfp
      width: 2rem
      height: 2rem

    .logo
      display: flex
      align-items: center
      gap: 8px
      font-weight: bold
      color: #e3e3e3
      letter-spacing: 0.05em
      flex: 1

      @media (max-width: vars.$w-mobile)
        > span
          display: none

    @media (max-width: vars.$w-mobile)
      justify-content: center

</style>
