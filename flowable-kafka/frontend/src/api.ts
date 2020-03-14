import { parseString } from 'xml2js';
import { promisify } from 'es6-promisify';

export interface Settings {
  speed: string;
}

export interface Station {
  id: string;
  name: string;
  position: number[];
  bikes: number;
  slots: number;
}

export type CommentData = {
  userId: string;
  stationId: number;
  rating: number;
  comment: string;
};

export type StationDict = { [id: string]: Station };

const parse = promisify(parseString);

// tslint:disable-next-line:no-any
const normalize = (station: any) => ({
  id: station.id[0],
  name: station.name[0],
  position: [parseFloat(station.long[0]), parseFloat(station.lat[0])],
  bikes: parseInt(station.nbBikes[0], 10),
  slots: parseInt(station.nbDocks[0], 10)
});

export const getCycleStations = () =>
  fetch('https://tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml')
    .then(res => res.text())
    .then(parse)
    .then((res: any) => res.stations.station.map(normalize))
    .then((stations: Station[]) =>
      // tslint:disable-next-line:no-object-literal-type-assertion
      stations.reduce((acc, station) => ((acc[station.id] = station), acc), {} as StationDict)
    );

export const sendComment = (comment: CommentData) =>
  fetch('/reviews', {
    method: 'POST',
    body: JSON.stringify(comment)
  });

export const getDashBoardData = () => fetch('/dashboard').then(res => res.json());
