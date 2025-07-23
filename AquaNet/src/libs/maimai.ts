import { AQUA_HOST, DATA_HOST } from './config'


export async function getMaimai(endpoint: string, params: any) {
  return await fetch(`${AQUA_HOST}/Maimai2Servlet/${endpoint}`, {
    method: 'POST',
    body: JSON.stringify(params)
  }).then(res => res.json())
}

export async function getMaimaiAllMusic(): Promise<{ [key: string]: any }> {
  return fetch(`${DATA_HOST}/maimai/meta/00/all-music.json`).then(it => it.json())
}

const jacketGlob = import.meta.glob('../resources/mai2res/Jacket/*.avif', { eager: true, import: 'default' })

export const getMaimaiJacket = (musicId: string | number) => {
  return jacketGlob[`../resources/mai2res/Jacket/00${musicId.toString().padStart(6, '0').substring(2)}.avif`] as string || 'invalid'
}

