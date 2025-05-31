<!-- Svelte 4.2.11 -->

<script lang="ts">
  import { slide } from "svelte/transition";
  import { t } from "../libs/i18n";
  import { type GameName, type ParsedComposition, roundFloor } from "../libs/scoring";
  import { coverNotFound } from "../libs/ui";
  import { tooltip } from "../libs/ui";
  import useLocalStorage from "../libs/hooks/useLocalStorage.svelte";

  export let game: GameName
  export let p: ParsedComposition
  const rounding = useLocalStorage("rounding", true)
</script>

<div class="map-detail-container" transition:slide>
  <div class="scores">
    <div>
      <img src={p.img} alt="" on:error={coverNotFound} />
      <div class="info">
        <div class="first-line">
          <div class="song-title">{p.name ?? t("UserHome.UnknownSong")}</div>
          <span class={`lv level-${p.diffId === 10 ? 3 : p.diffId}`}>
            { p.difficulty ?? '-' }
          </span>
        </div>
        <div class="second-line">
          <span class={`rank-${p.rank[0]}`}>
            <span class="rank-text">{p.rank.replace("p", "+")}</span>
            <span class="rank-num" use:tooltip={(p.score / 10000).toFixed(4)}>
              {rounding.value ? roundFloor(p.score, game, 1) : (p.score / 10000).toFixed(4)}%
            </span>
          </span>
          {#if p.ratingChange !== undefined}
            <span class="dx-change">{ p.ratingChange }</span>
          {/if}
        </div>
      </div>
    </div>
  </div>
</div>

<style lang="sass">

  @use "../vars"
  vars.$gap: 20px

  .map-detail-container
    background-color: rgb(35,35,35)
    border-radius: vars.$border-radius
    overflow: hidden

    .scores
      display: flex
      flex-direction: column
      flex-wrap: wrap
      gap: vars.$gap

      // Image and song info
      > div
        display: flex
        align-items: center
        gap: 12px
        max-width: 100%
        box-sizing: border-box

        img
          width: 50px
          height: 50px
          border-radius: vars.$border-radius
          object-fit: cover

        // Song info and score
        > div.info
          flex: 1
          display: flex
          justify-content: space-between
          overflow: hidden
          flex-direction: column

          .first-line
            display: flex
            flex-direction: row

          // Limit song name to one line
          .song-title
            flex: 1
            min-width: 0
            overflow: hidden
            text-overflow: ellipsis
            white-space: nowrap

          // Make song score and rank not wrap
          > div:last-child
            white-space: nowrap

          @media (max-width: vars.$w-mobile)
            flex-direction: column
            gap: 0

            .rank-text
              text-align: left

        .rank-S
          // Gold green gradient on text
          background: vars.$grad-special
          -webkit-background-clip: text
          color: transparent

        .rank-A
          color: #ff8a8a

        .rank-B
          color: #6ba6ff

        .lv
          width: 30px
          text-align: center
          background: rgba(var(--lv-color), 0.6)
          padding: 0 6px
          border-radius: 0 vars.$border-radius 0 vars.$border-radius

          // Inset shadow, like it's a paper below this card with a cut
          box-shadow: inset 0 0 10px rgba(0,0,0,0.5)

        span
          display: inline-block
          text-align: left

        .second-line
          display: flex
          justify-content: space-between
          align-items: center

        // Vertical table-like alignment
        span.rank-text
          min-width: 40px
        span.rank-num
          min-width: 60px
        span.dx-change
          margin-right: 0.5rem
          color: vars.$c-good
</style>
