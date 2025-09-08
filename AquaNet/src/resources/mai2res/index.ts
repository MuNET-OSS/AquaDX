import padId from '@/utils/padId';

export default {
  async jacket(id: number): Promise<string> {
    id = id % 1e4;
    const { default: jacketGlob } = await import('./jacketGlob');
    return jacketGlob[`./Jacket/${padId(id, 6)}.avif`] as string;
  },
  async musicById(id: number) {
    const { default: all } = await import ('./all-music.json');
    return all[id as any as keyof typeof all];
  },
  async musicIdByName(name: string) {
    const { default: all } = await import ('./all-music.json');
    const musicId = Object.keys(all).find(key => all[key as any as keyof typeof all].name === name);
    if (!musicId) {
      return 0;
    }
    return parseInt(musicId);
  },
};
