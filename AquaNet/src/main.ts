import { mount } from 'svelte';
import './app.sass'
import App from './App.svelte'
import './assets/fonts/Quicksand.400.css'
import './assets/fonts/Quicksand.500.css'

const app = mount(App, { target: document.getElementById("app")! });

export default app
