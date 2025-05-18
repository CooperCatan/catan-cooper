import React from 'react';
import { cn } from '../../utils/cn';

// import images directly
import brickCardImg from '../../assets/images/resource-cards/brick-card.png';
import woodCardImg from '../../assets/images/resource-cards/wood-card.png';
import oreCardImg from '../../assets/images/resource-cards/ore-card.png';
import wheatCardImg from '../../assets/images/resource-cards/wheat-card.png';
import sheepCardImg from '../../assets/images/resource-cards/sheep-card.png';

interface ResourceCardsDisplayProps {
  resources: {
    brick?: number;
    wood?: number;
    ore?: number;
    wheat?: number;
    sheep?: number;
    [key: string]: number | undefined; // Allow other potential resources, though we only map the main 5
  };
  className?: string;
}

// Use imported images
const resourceImageMap: { [key: string]: string } = {
  brick: brickCardImg,
  wood: woodCardImg,
  ore: oreCardImg,
  wheat: wheatCardImg,
  sheep: sheepCardImg,
};

const resourceOrder: (keyof ResourceCardsDisplayProps['resources'])[] = ['brick', 'wood', 'ore', 'wheat', 'sheep'];

const ResourceCardsDisplay: React.FC<ResourceCardsDisplayProps> = ({ resources, className }) => {
  if (!resources) {
    return null;
  }

  return (
    <div className={cn("flex flex-wrap justify-center gap-2", className)}>
      {resourceOrder.map((resourceName) => {
        const count = resources[resourceName];
        const imagePath = resourceImageMap[resourceName];

        if (count && count > 0 && imagePath) {
          return (
            <div key={resourceName} className="relative transform transition-transform hover:scale-105">
              <img 
                src={imagePath} 
                alt={`${resourceName} card`} 
                className="w-12 h-auto md:w-16 rounded-md shadow-md border border-gray-300" 
              />
              <div className="absolute -top-1 -right-1 bg-blue-500 text-white text-xs font-bold w-5 h-5 rounded-full flex items-center justify-center shadow-sm">
                {count}
              </div>
            </div>
          );
        }
        return null;
      })}
    </div>
  );
};

export default ResourceCardsDisplay; 