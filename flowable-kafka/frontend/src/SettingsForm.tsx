import React, { useState, FormEvent, useCallback } from 'react';

import { SettingsData } from './model';
import { Settings } from './api';

import './settingsForm.scss';

type settingFormProps = {
  settings: Settings;
  setSpeed: (speed: string) => void;
};

const speedSettings = {
  lazySunday: 'A lazy sunday morning',
  franticFriday: 'A frantic friday evening',
  freezeTime: 'Freeze the mists of time'
};

export const SettingsForm = (props: settingFormProps) => {
  let [formData, setFormData] = useState<SettingsData>({
    speed: props.settings.speed
  });

  const onSpeedChange = useCallback(
    (e: FormEvent<HTMLInputElement>) => {
      setFormData({ ...formData, speed: e.currentTarget.value });
      props.setSpeed(e.currentTarget.value);
    },
    [formData]
  );

  return (
    <div className="flw-settingsForm-form">
      <span className="flw-settingsForm-form__title">Settings</span>
      <div>
        <div className="flw-settingsForm-form__label">Demo speed</div>
        {Object.keys(speedSettings).map(s => (
          <div key={`key-${s}`} className="flw-settingsForm-form__input">
            <label>
              <input type="checkbox" onChange={onSpeedChange} checked={formData.speed === s} value={s} />
              {(speedSettings as any)[s]}
            </label>
          </div>
        ))}
      </div>
    </div>
  );
};
