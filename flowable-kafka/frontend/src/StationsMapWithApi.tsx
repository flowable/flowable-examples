import React, { useEffect, useState, useCallback } from 'react';
import faker from 'faker';

import { getCycleStations, Station, StationDict, sendComment, Settings } from './api';
import { StationsMap } from './StationsMap';
import { MarkerProps, Comment } from './model';
import commentsJson from './comments.json';
import { SettingsForm } from './SettingsForm';

export const StationsMapWithApi = () => {
  const [settings, setSettings] = useState<Settings>({ speed: 'freezeTime' });
  const [stations, setStations] = useState<Station[]>([]);
  const [comments, setComments] = useState<Array<MarkerProps>>([]);
  let [count, setCount] = useState<number>(1);

  useEffect(() => {
    getCycleStations().then((res: StationDict) => {
      let stationsRes = Object.keys(res).map(k => res[k]);
      setStations(stationsRes);
    });
  }, []);

  useEffect(() => {
    let timeout = 2000;
    if (settings.speed === 'franticFriday') {
      timeout = 10;
    }

    var timerID = setInterval(() => {
      if (settings.speed !== 'freezeTime') {
        setCount(count + 1);
      }
    }, timeout);

    return function cleanup() {
      clearInterval(timerID);
    };
  });

  useEffect(() => {
    if (stations.length) {
      const newComments = [...comments];
      if (newComments.length > 1) {
        newComments.shift();
      }

      const c = Math.floor(Math.random() * commentsJson.length - 1);
      addCommentToStation(commentsJson[c]).then((marker: MarkerProps | null) => {
        marker && newComments.push(marker);
        setComments(newComments);
      });
    }
  }, [stations, count]);

  const addCommentToStation = useCallback(
    async (comment: Comment, addAvatar = true): Promise<MarkerProps | null> => {
      if (comment) {
        const s = comment.station ? comment.station : Math.floor(Math.random() * (stations.length - 1));
        const marker: MarkerProps = {
          id: s,
          coordinates: stations[s].position,
          comment: { ...comment, station: s, avatarUrl: addAvatar ? faker.image.avatar() : undefined }
        };

        // Backend call happens here
        try {
          const res = await sendComment({
            userId: comment.user,
            stationId: comment.station || 0,
            rating: comment.rating,
            comment: comment.comment || ''
          });
          if (res.ok) {
            return marker;
          }
          throw new Error(`code: ${res.status}, message: ${res.statusText}`);
        } catch (error) {
          console.warn(error);
          return null;
        }
      }

      return null;
    },
    [stations]
  );

  const onCommentSubmit = useCallback(
    async (comment: Comment) => {
      const marker: MarkerProps | null = await addCommentToStation(comment, false);
      marker && setComments([...comments, marker]);
    },
    [comments]
  );

  return (
    <>
      <StationsMap onFormSubmit={onCommentSubmit} stations={stations} comments={comments} />
      <SettingsForm settings={settings} setSpeed={speed => setSettings({ speed })} />>
    </>
  );
};
