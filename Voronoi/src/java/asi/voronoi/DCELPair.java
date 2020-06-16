package asi.voronoi;

class DCELPair implements java.io.Serializable {
    DCEL currentElem;
    DCEL nextElem;
    DCEL cutBy;

    DCELPair(DCEL current, DCEL cut, DCEL next) {
        currentElem = current;
        cutBy = cut;
        nextElem = next;
    }
}
