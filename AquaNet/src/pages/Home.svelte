<script lang="ts">
  import { fade } from "svelte/transition";
  import LinkCard from "./Home/LinkCard.svelte";
  import SetupInstructions from "./Home/SetupInstructions.svelte";
  import { DISCORD_INVITE, FADE_IN, FADE_OUT } from "../libs/config";
  import { USER } from "../libs/sdk.js";
  import type { AquaNetUser } from "../libs/generalTypes";
  import StatusOverlays from "../components/StatusOverlays.svelte";
  import ActionCard from "../components/ActionCard.svelte";
  import { t } from "../libs/i18n";
  import ImportDataAction from "./Home/ImportDataAction.svelte";
  import Communities from "./Home/Communities.svelte";

  USER.ensureLoggedIn();

  let me: AquaNetUser
  let error = ""

  let tab = 0;
  let tabs = [t('home.nav.portal')]

  USER.me().then((m) => me = m).catch(e => error = e.message)
</script>

<main class="content">
<!--  <h2 class="outer-title">&nbsp;</h2>-->
  <nav class="tabs">
    {#each tabs as t, i}
      <div class="clickable"
           class:active={tab === i}
           on:click={() => tab = i}
           on:keydown={(e) => e.key === "Enter" && (tab = i)}
           role="button" tabindex={i}>{t}
      </div>
    {/each}
  </nav>

  {#if tab === 0}
    <div out:fade={FADE_OUT} in:fade={FADE_IN} class="action-cards">
      <ImportDataAction/>
    </div>
  {/if}
</main>

<StatusOverlays {error} loading={!me}/>

<style lang="sass">
  @use "../vars"

  .tabs
    display: flex
    gap: 1rem

    div
      &.active
        color: vars.$c-main

  h3
    font-size: 1.3rem
    margin: 0

  .action-cards
    display: flex
    flex-direction: column
    gap: 1rem
</style>
